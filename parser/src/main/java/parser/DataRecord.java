package parser;

import java.util.Arrays;

/**
 * PHYSICAL RECORD (NEW STYLE) | length of first variable-length field of data | | SQL-null flags (1
 * bit per nullable field), padded to full bytes | | 1 or 2 bytes to indicate number of fields in
 * the record if the table where the record resides has undergone an instant ADD COLUMN before this
 * record gets inserted; If no instant ADD COLUMN ever happened, here should be no byte; So parsing
 * this optional number requires the index or table information | | 4 bits used to delete mark a
 * record, and mark a predefined minimum record in alphabetical order | | 4 bits giving the number
 * of records owned by this record (this term is explained in page0page.h) | | 13 bits giving the
 * order number of this record in the heap of the index page | | 3 bits record type:
 * 000=conventional, 001=node pointer (inside B-tree), 010=infimum, 011=supremum, 1xx=reserved | |
 * two bytes giving a relative pointer to the next record in the page | ORIGIN of the record | first
 * field of data | ... | last field of data |
 */
public class DataRecord {
    private final short next;
    private final RecordInfo[] info;
    private final short nOwned;
    private final short Order;
    private final RecordType type;

    public int[] variableColumnLengths;
    public boolean[] nulls;
    public byte[] rawData;
    public boolean externalData;

    public DataRecord(RecordInfo[] info, short nOwned, short Order, RecordType type, short next) {
        this.info = info;
        this.nOwned = nOwned;
        this.Order = Order;
        this.type = type;
        this.next = next;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DataRecord{");
        sb.append("next=").append(next);
        sb.append(", info=").append(info == null ? "null" : Arrays.asList(info).toString());
        sb.append(", nOwned=").append(nOwned);
        sb.append(", Order=").append(Order);
        sb.append(", type=").append(type);
        sb.append(", variableColumnLengths=");
        if (variableColumnLengths == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < variableColumnLengths.length; ++i)
                sb.append(i == 0 ? "" : ", ").append(variableColumnLengths[i]);
            sb.append(']');
        }
        sb.append(", nulls=");
        if (nulls == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < nulls.length; ++i) sb.append(i == 0 ? "" : ", ").append(nulls[i]);
            sb.append(']');
        }
        sb.append(", externalData=").append(externalData);
        sb.append('}');
        return sb.toString();
    }

    public RecordInfo[] info() {
        return info;
    }

    public short nOwned() {
        return nOwned;
    }

    public short Order() {
        return Order;
    }

    public RecordType type() {
        return type;
    }

    public short next() {
        return next;
    }
}
