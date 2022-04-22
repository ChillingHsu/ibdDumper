package parser;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class RecordDirectory {
    final String name;
    final boolean nullable;
    final boolean variable;
    final int length;
    final Class<?> targetClass;
    final Function<byte[], Object> converter;

    private RecordDirectory(
            String name,
            boolean nullable,
            boolean variable,
            int length,
            Class<?> targetClass,
            Function<byte[], Object> converter) {
        this.name = name;
        this.nullable = nullable;
        this.variable = variable;
        this.length = length;
        this.targetClass = targetClass;
        this.converter = converter;
    }

    public static RecordDirectory VariableColumn(
            String name,
            boolean nullable,
            Class<?> targetClass,
            Function<byte[], Object> converter) {
        return new RecordDirectory(name, nullable, true, Integer.MIN_VALUE, targetClass, converter);
    }

    public static RecordDirectory FixedColumn(
            String name,
            boolean nullable,
            int length,
            Class<?> targetClass,
            Function<byte[], Object> converter) {
        return new RecordDirectory(name, nullable, false, length, targetClass, converter);
    }

    public static Function<byte[], Object> IntegerConverter =
            bytes -> {
                if (bytes == null) return null;
                if (bytes.length > Univ.SIZEOF_UINT32)
                    throw new IllegalStateException("bytes length exceeds");
                return ByteBuffer.allocate(Univ.SIZEOF_UINT32)
                        .put(Univ.SIZEOF_UINT32 - bytes.length, bytes)
                        .rewind()
                        .getInt();
            };
    public static Function<byte[], Object> LongConverter =
            bytes -> {
                if (bytes == null) return null;
                if (bytes.length > Univ.SIZEOF_UINT64)
                    throw new IllegalStateException("bytes length exceeds");
                return ByteBuffer.allocate(Univ.SIZEOF_UINT64)
                        .put(Univ.SIZEOF_UINT64 - bytes.length, bytes)
                        .rewind()
                        .getLong();
            };
    public static Function<byte[], Object> StringConverter =
            bytes -> bytes == null ? null : new String(bytes);
}
