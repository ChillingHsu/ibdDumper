/* (C)2022 */
package parser;

import java.nio.ByteBuffer;

public enum XdesState {
    /** extent descriptor is not initialized */
    NOT_INITED(0),

    /** extent is in free list of space */
    FREE(1),

    /** extent is in free fragment list of space */
    FREE_FRAG(2),

    /** extent is in full fragment list of space */
    FULL_FRAG(3),

    /** extent belongs to a segment */
    FSEG(4),

    /** fragment extent leased to segment */
    FSEG_FRAG(5);

    private final int code;

    XdesState(int code) {
        this.code = code;
    }

    public static XdesState getByCode(final int code) {
        for (XdesState state : XdesState.values()) {
            if (state.code == code) return state;
        }
        return null;
    }

    public static XdesState fromByteArray(final byte[] bytes) {
        return getByCode(ByteBuffer.wrap(bytes).getInt());
    }
}
