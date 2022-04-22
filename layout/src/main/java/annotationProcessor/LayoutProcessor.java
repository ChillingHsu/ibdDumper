package annotationProcessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({"annotationProcessor.Length", "annotationProcessor.Sublayout"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class LayoutProcessor extends AbstractProcessor {
    class LayoutField {
        Sublayout sublayout = null;
        int length;
        String name;
        TypeMirror targetType;

        public LayoutField(Element field) {
            this(field, field.getAnnotation(Length.class).value());
        }

        public LayoutField(Element field, int length) {
            this.length = length;
            this.name = field.toString();
            this.targetType = typeUtils.erasure(field.asType());
        }

        public LayoutField(Element field, int length, Sublayout sublayout) {
            this(field, length);
            this.sublayout = sublayout;
        }

        @Override
        public String toString() {
            return String.format(
                    "LayoutField{%d Byte(s) Field %s -> %s}", length, name, targetType);
        }
    }

    Types typeUtils;
    ArrayType byteArray;
    TypeMirror boxedShort;
    TypeMirror boxedInteger;
    TypeMirror boxedLong;
    Map<String, OptionalInt> layoutLengthMap;

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        typeUtils = processingEnv.getTypeUtils();
        byteArray = typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.BYTE));
        boxedShort = typeUtils.boxedClass(typeUtils.getPrimitiveType(TypeKind.SHORT)).asType();
        boxedInteger = typeUtils.boxedClass(typeUtils.getPrimitiveType(TypeKind.INT)).asType();
        boxedLong = typeUtils.boxedClass(typeUtils.getPrimitiveType(TypeKind.LONG)).asType();
        // roundEnvironment.getRootElements().stream().map(element ->
        // element.getAnnotationMirrors())
        Set<? extends Element> elements =
                roundEnvironment.getElementsAnnotatedWithAny(Set.of(Length.class, Sublayout.class));
        if (elements.isEmpty()) return false;
        Map<String, ? extends List<? extends Element>> layoutFieldsMap =
                elements.stream()
                        .collect(
                                Collectors.groupingBy(
                                        element -> {
                                            return element.getEnclosingElement()
                                                            .getEnclosingElement()
                                                            .getSimpleName()
                                                    + "."
                                                    + element.getEnclosingElement().getSimpleName();
                                        }));
        layoutLengthMap =
                layoutFieldsMap.keySet().stream()
                        .collect(
                                Collectors.toConcurrentMap(
                                        Function.identity(), key -> OptionalInt.empty()));
        layoutLengthMap.forEach(
                (qualifiedName, length) -> {
                    if (length.isEmpty()) resolveLayout(layoutFieldsMap, qualifiedName);
                });
        return true;
    }

    private void resolveLayout(
            Map<String, ? extends List<? extends Element>> layoutFieldsMap, String qualifiedName) {
        if (layoutLengthMap.get(qualifiedName).isPresent()) return;
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        String packageName = qualifiedName.substring(0, lastDotIndex);
        String superclassName = qualifiedName.substring(lastDotIndex + 1);
        if (!superclassName.endsWith("Layout")) {
            processingEnv
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR, "Abstract class name must end with \"Layout\"");
            throw new RuntimeException();
        }
        String className = superclassName.substring(0, superclassName.indexOf("Layout"));

        List<String> unresolvedSublayout =
                layoutFieldsMap.get(qualifiedName).stream()
                        .filter(
                                field -> {
                                    Sublayout annotation = field.getAnnotation(Sublayout.class);
                                    return isRepeatedSublayout(annotation)
                                            || isPlainSublayout(annotation);
                                })
                        .<String>mapMulti(
                                (field, consumer) -> {
                                    TypeMirror type = field.asType();
                                    type = typeUtils.erasure(type);
                                    consumer.accept(type.toString());
                                    if (type.getKind() == TypeKind.ARRAY
                                            && isRepeatedSublayout(
                                                    field.getAnnotation(Sublayout.class))) {
                                        consumer.accept(getComponentTypeOf(type).toString());
                                    }
                                })
                        .filter(
                                fieldType ->
                                        layoutLengthMap.containsKey(fieldType)
                                                && layoutLengthMap.get(fieldType).isEmpty())
                        .toList();

        unresolvedSublayout.forEach(
                s -> {
                    resolveLayout(layoutFieldsMap, s);
                });

        List<LayoutField> fields =
                layoutFieldsMap.get(qualifiedName).stream()
                        .map(
                                field -> {
                                    Sublayout sublayoutAnnotation =
                                            field.getAnnotation(Sublayout.class);
                                    if (field.getAnnotation(Length.class) != null) {
                                        return new LayoutField(field);
                                    } else {
                                        TypeMirror erasedType = typeUtils.erasure(field.asType());
                                        if (isPlainSublayout(sublayoutAnnotation)) {
                                            return new LayoutField(
                                                    field, getFromFieldsMapAsInt(erasedType));
                                        } else if (isLengthSublayout(sublayoutAnnotation)) {
                                            return new LayoutField(
                                                    field,
                                                    sublayoutAnnotation.length(),
                                                    sublayoutAnnotation);
                                        } else if (isRepeatedSublayout(sublayoutAnnotation)) {
                                            return new LayoutField(
                                                    field,
                                                    sublayoutAnnotation.repeated()
                                                            * getFromFieldsMapAsInt(
                                                                    getComponentTypeOf(erasedType)),
                                                    sublayoutAnnotation);
                                        } else {
                                            processingEnv
                                                    .getMessager()
                                                    .printMessage(
                                                            Diagnostic.Kind.ERROR,
                                                            "Unexpected error");
                                            throw new RuntimeException();
                                        }
                                    }
                                })
                        .toList();
        int size = generateClassFile(packageName, className, superclassName, fields);
        layoutLengthMap.put(qualifiedName, OptionalInt.of(size));
    }

    private int getFromFieldsMapAsInt(TypeMirror field) {
        OptionalInt anInt = layoutLengthMap.get(field.toString());
        if (anInt.isEmpty()) {
            throw new RuntimeException("Unexpected field type: " + field);
        }
        return anInt.getAsInt();
    }

    private TypeMirror getComponentTypeOf(TypeMirror arrayType) {
        if (arrayType instanceof ArrayType type) return type.getComponentType();
        else throw new RuntimeException("Unexpected TypeMirror instance: " + arrayType);
    }

    private boolean isRepeatedSublayout(Sublayout annotation) {
        return annotation != null && annotation.repeated() >= 1;
    }

    private boolean isLengthSublayout(Sublayout annotation) {
        return annotation != null && annotation.length() >= 0;
    }

    private boolean isPlainSublayout(Sublayout annotation) {
        return annotation != null && annotation.length() == -1 && annotation.repeated() == -1;
    }

    private int generateClassFile(
            String packageName, String className, String superclassName, List<LayoutField> fields) {
        StringBuilder constructorContent = new StringBuilder();
        ArrayList<String> offsetMapEntries = new ArrayList<>();
        int size = 0;
        for (LayoutField field : fields) {
            offsetMapEntries.add(
                    String.format(
                            "Map.entry(%d, self.getDeclaredField(\"%s\"))", size, field.name));
            size += field.length;
            if (typeUtils.isSameType(byteArray, field.targetType)) {
                constructorContent.append(
                        String.format(
                                "byteBuffer.get(this.%s = new byte[%d]);",
                                field.name, field.length));
            } else if (typeUtils.isSameType(boxedLong, field.targetType)
                    || typeUtils.isSameType(boxedInteger, field.targetType)
                    || typeUtils.isSameType(boxedShort, field.targetType)) {
                String template =
                        switch (field.length) {
                            case 2 -> {
                                if (!typeUtils.isSameType(boxedShort, field.targetType))
                                    yield "this.%s = Short.toUnsignedInt(byteBuffer.getShort());";
                                else yield "this.%s = byteBuffer.getShort();";
                            }
                            case 4 -> {
                                if (typeUtils.isSameType(boxedShort, field.targetType)) {
                                    throw new RuntimeException(
                                            "Not compatible type: trying to store 4 bytes in a"
                                                    + " Short");
                                } else if (typeUtils.isSameType(boxedInteger, field.targetType)) {
                                    yield "this.%s = byteBuffer.getInt();";
                                } else {
                                    yield "this.%s = Integer.toUnsignedLong(byteBuffer.getInt());";
                                }
                            }
                            case 8 -> {
                                if (typeUtils.isSameType(boxedShort, field.targetType)) {
                                    throw new RuntimeException(
                                            "Not compatible type: trying to store 8 bytes in a"
                                                    + " Short");
                                } else if (typeUtils.isSameType(boxedInteger, field.targetType)) {
                                    throw new RuntimeException(
                                            "Not compatible type: trying to store 8 bytes in a"
                                                    + " Integer");
                                } else {
                                    yield "this.%s = byteBuffer.getLong();";
                                }
                            }
                            default -> throw new IllegalStateException(
                                    "Unexpected value: " + field.length);
                        };
                constructorContent.append(String.format(template, field.name));

            } else if (layoutLengthMap.containsKey(field.targetType.toString())
                    && (field.sublayout == null || field.sublayout.args().length == 0)) {
                String targetTypeQualifiedLayoutName = field.targetType.toString();
                String targetTypeLayoutName =
                        targetTypeQualifiedLayoutName.substring(
                                targetTypeQualifiedLayoutName.lastIndexOf('.') + 1);
                String targetType =
                        targetTypeLayoutName.substring(
                                0, targetTypeLayoutName.lastIndexOf("Layout"));
                constructorContent.append(
                        String.format(
                                "this.%s = new %sGen.%s(byteBuffer);%n",
                                field.name, packageName, targetType));
            } else if (field.targetType.getKind() == TypeKind.ARRAY
                    && field.sublayout != null
                    && field.sublayout.repeated() >= 1) {
                // size += field.length * (field.sublayout.repeated() - 1);
                String componentLayoutType = getComponentTypeOf(field.targetType).toString();
                String componentType =
                        componentLayoutType.substring(
                                componentLayoutType.lastIndexOf('.') + 1,
                                componentLayoutType.lastIndexOf("Layout"));
                int arrayLength = field.sublayout.repeated();
                String template =
                        """
                        {
                            this.#{fieldName} = new #{componentType}[#{arrayLength}];
                            for(int i = 0; i < #{arrayLength}; ++i) {
                                this.#{fieldName}[i] = new #{componentType}(byteBuffer);
                            }
                        }
                        """;
                String code =
                        template.replaceAll("#\\{fieldName}", field.name)
                                .replaceAll("#\\{componentType}", componentType)
                                .replaceAll("#\\{arrayLength}", String.valueOf(arrayLength));
                constructorContent.append(code);
            } else if (field.sublayout != null) {
                constructorContent.append(
                        String.format("this.%s = %s.%s(", field.name, superclassName, field.name));
                for (String arg : field.sublayout.args()) {
                    constructorContent.append(String.format("%s, ", arg));
                }
                constructorContent.append("byteBuffer);");
            } else {
                constructorContent.append(
                        String.format(
                                """
                        {
                            byte[] bytes = new byte[%d];
                            byteBuffer.get(bytes);
                            this.%s = %s.fromByteArray(bytes);
                        }
                        """,
                                field.length, field.name, field.targetType));
            }
        }

        String staticContent =
                String.format(
                        """
                   static {
                       final Class<%s> self = %s.class;
                       try {
                           offsetFieldMap = Map.ofEntries(%s);
                       } catch (Exception e) {
                           throw new RuntimeException("Unexpected error during generated code for class %s: " + e);
                       }
                   }""",
                        superclassName,
                        superclassName,
                        String.join(", ", offsetMapEntries),
                        className);

        Filer filer = processingEnv.getFiler();

        try (PrintWriter writer =
                new PrintWriter(
                        filer.createSourceFile(packageName + "Gen." + className).openWriter())) {
            writer.printf(
                    """
                            package %sGen;

                            import java.io.IOException;
                            import java.nio.ByteBuffer;
                            import java.nio.channels.ReadableByteChannel;
                            import java.util.Map;
                            import java.lang.reflect.Field;
                            import %s.%s;
                            public class %s extends %s {
                                public static final int layoutSize = %d;
                                private static final Map<Integer, Field> offsetFieldMap;
                                %s
                                public %s(ReadableByteChannel channel) throws IOException {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(layoutSize);
                                    if (channel.read(byteBuffer) != layoutSize)
                                        throw new IOException("cannot read enough data");
                                    byteBuffer.rewind();
                                    %s
                                }
                                public %s(ByteBuffer byteBuffer) {
                                    %s
                                }

                                @Override
                                protected Map<Integer, Field> getOffsetFieldMap() {
                                    return %s.offsetFieldMap;
                                }

                                @Override
                                protected int getLayoutSize() {
                                    return %s.layoutSize;
                                }
                            }
                            """,
                    packageName,
                    packageName,
                    superclassName,
                    className,
                    superclassName,
                    size,
                    staticContent,
                    className,
                    constructorContent,
                    className,
                    constructorContent,
                    className,
                    className);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }
}
