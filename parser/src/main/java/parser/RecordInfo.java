package parser;

import java.util.ArrayList;

public enum RecordInfo {
    MIN_REC(0x10),
    DELETED(0x20),
    INSTANT(0x80);

    private final short code;

    RecordInfo(int code) {
        this.code = (short) code;
    }

    public static RecordInfo[] getByCode(short code) {
        ArrayList<RecordInfo> recordInfo = new ArrayList<>(3);
        for (RecordInfo value : RecordInfo.values()) {
            if ((value.code & code) != 0) recordInfo.add(value);
        }
        RecordInfo[] result = new RecordInfo[recordInfo.size()];
        recordInfo.toArray(result);
        return result;
    }
}
