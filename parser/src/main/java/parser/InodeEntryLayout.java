package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Formatter;

public abstract class InodeEntryLayout extends Layout {
    public static int MAGIC_NUMBER = 97937874;
    /** 8 bytes of segment id: if this is 0, it means that the header is unused */
    @Length(8)
    protected Long segId;
    /** number of used segment pages in the FSEG_NOT_FULL list */
    @Length(4)
    protected Integer notFullNUsed;

    @Sublayout protected FlstBnodeLayout free;
    @Sublayout protected FlstBnodeLayout notFull;
    @Sublayout protected FlstBnodeLayout full;

    @Length(4)
    protected Integer magicNumber;

    @Sublayout(repeated = 32)
    protected PageNoLayout[] fragArray; // long for unsigned int

    @Override
    public void compactFormat(Formatter formatter) {

        if (segId == 0) {
            formatter.item("<InodeEntry SEG#UNINIT>");
            return;
        }
        formatter.item("<InodeEntry SEG#" + segId + ">");
    }

    @Override
    public void format(Formatter formatter) {
        if (segId == 0) {
            formatter.item("<Uninitialized InodeEntry>");
            return;
        }
        formatter.item("<InodeEntry>");
        formatter.child();
        formatter.item("SEG ID: " + segId);
        formatter.item("FREE Extents list: " + free);
        formatter.item("NOT-FULL Extents list (" + notFullNUsed + " page(s)): " + notFull);
        formatter.item("FULL Extents list: " + full);

        if (fragArray == null) formatter.item("Null Fragment Array");
        else {
            formatter.item("Fragment Array: ");
            int nRow = 4;
            for (int i = 0; i < nRow; ++i) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < fragArray.length / nRow; ++j) {
                    sb.append(String.format("%-4s ", fragArray[i * nRow + j]));
                }
                formatter.subItem(f -> f.item(sb.toString().trim()));
            }
        }
        formatter.parent();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("InodeEntryLayout{");
        sb.append("segId=").append(segId);
        sb.append(", notFullNUsed=").append(notFullNUsed);
        sb.append(", free=").append(free);
        sb.append(", notFull=").append(notFull);
        sb.append(", full=").append(full);
        sb.append(", fragArray=");
        if (fragArray == null) sb.append("null");
        else {
            sb.append('[');
            for (int i = 0; i < fragArray.length; ++i)
                sb.append(i == 0 ? "" : " ").append(fragArray[i].isNullPage() ? "N" : fragArray[i]);
            sb.append(']');
        }
        sb.append('}');
        return sb.toString();
    }
}
