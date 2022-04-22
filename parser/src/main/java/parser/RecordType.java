package parser;
/** 000=conventional, 001=node pointer (inside B-tree), 010=infimum, 011=supremum, 1xx=reserved */
public enum RecordType {
    ORDINARY(0b000),
    NODE_POINTER(0b001),
    INFIMUM(0b010),
    SUPREMUM(0b011),
    RESERVED(0b100);

    private final short code;

    RecordType(int code) {
        this.code = (short) code;
    }

    public static RecordType getByCode(short code) {
        for (RecordType value : RecordType.values()) {
            if (value.code == code) return value;
        }
        if (code > RESERVED.code) return RESERVED;
        throw new RuntimeException("Unknown record type: " + code);
    }
}
