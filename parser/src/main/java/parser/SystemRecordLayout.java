package parser;

import annotationProcessor.Length;

public abstract class SystemRecordLayout extends Record {
    @Length(3)
    protected byte[] rawMeta;

    @Length(2)
    protected Short nextRecordOffset;

    @Length(8)
    protected byte[] literature;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SystemRecord{");
        sb.append("rawMeta=");
        if (rawMeta == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < rawMeta.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(rawMeta[i]);
            sb.append(']');
        }
        sb.append(", nextRecordOffset=").append(nextRecordOffset);
        sb.append(", ").append(new String(literature));
        sb.append('}');
        return sb.toString();
    }
}
