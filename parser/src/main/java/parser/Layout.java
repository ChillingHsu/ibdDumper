/* (C)2022 */
package parser;

import formatter.Formatter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public abstract class Layout {
    protected abstract Map<Integer, Field> getOffsetFieldMap();

    protected abstract int getLayoutSize();

    public void format(Formatter formatter) {
        formatter.item(this.toString());
    }

    public void compactFormat(Formatter formatter) {
        format(formatter);
    }

    public final void format(Formatter formatter, boolean compact) {
        if (compact) compactFormat(formatter);
        else format(formatter);
    }

    public <T> T offsetAt(int targetOffset, final Class<T> targetClass, boolean precise) {
        if (targetClass.isAssignableFrom(this.getClass()) && (!precise || targetOffset == 0)) {
            return targetClass.cast(this);
        }
        Integer position =
                getOffsetFieldMap().keySet().stream()
                        .filter(o -> o <= targetOffset)
                        .max(Integer::compareTo)
                        .get();
        Field targetField =
                Objects.requireNonNull(getOffsetFieldMap().get(position), "Invalid target offset");
        int deltaOffset = targetOffset - position;
        Object fieldValue = null;
        try {
            fieldValue = targetField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get data from this field: " + e);
        }
        if (fieldValue instanceof Layout layout) { // case 'sublayout': propagate it
            return layout.offsetAt(deltaOffset, targetClass, precise);
        } else if (fieldValue.getClass().isArray()) { // case 'someThing[]': calc the index
            if (fieldValue instanceof byte[] bytes) {
                return targetClass.isAssignableFrom(Byte.class)
                        ? targetClass.cast(bytes[targetOffset - position])
                        : null;
            } else if (fieldValue instanceof Layout[] layouts) {
                int layoutSize = layouts[0].getLayoutSize();
                assert layouts.length > deltaOffset / layoutSize;
                return layouts[deltaOffset / layoutSize].offsetAt(
                        deltaOffset % layoutSize, targetClass, precise);
            } else {
                throw new RuntimeException("Unsupported array type: " + fieldValue.getClass());
            }
        } else { // case 'Integer'/'Long'/...: return it
            return targetClass.isAssignableFrom(fieldValue.getClass())
                    ? targetClass.cast(fieldValue)
                    : null;
        }
    }
}
