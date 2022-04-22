package parser;

import java.nio.ByteBuffer;

/* Directions of cursor movement */
// mysql-server/storage/innobase/include/page0types.h
public enum InsertionDirection {
    PAGE_LEFT(1),
    PAGE_RIGHT(2),
    PAGE_SAME_REC(3),
    PAGE_SAME_PAGE(4),
    PAGE_NO_DIRECTION(5);

    final int code;

    InsertionDirection(int code) {
        this.code = code;
    }

    public static InsertionDirection getByCode(int code) {
        for (final InsertionDirection value : InsertionDirection.values()) {
            if (value.code == code) return value;
        }
        return null;
    }

    public static InsertionDirection fromByteArray(final byte[] bytes) {
        return getByCode(ByteBuffer.wrap(bytes).getShort());
    }
}
