/* (C)2022 */
package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class XdesEntryLayout extends Layout {
    /** The identifier of the segment to which this extent belongs */
    @Length(8)
    protected Long segId;

    /** The list node data structure for the descriptors */
    @Sublayout protected FlstNodeLayout node;

    /* contains state information of the extent */
    @Length(4)
    protected XdesState state;

    /* Descriptor bitmap of the pages in the extent */
    @Sublayout(length = 16)
    protected XdesBits[] bitmap;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Extent{");
        sb.append("state=").append(state);
        if (state == XdesState.FSEG || state == XdesState.FSEG_FRAG)
            sb.append("(SEG #").append(segId).append(")");
        sb.append(", bitmap=")
                .append(
                        bitmap == null
                                ? "null"
                                : Arrays.stream(bitmap)
                                        .map(bits -> bits == XdesBits.FREE ? "F" : "-")
                                        .collect(Collectors.joining()));
        sb.append(", node=").append(node);
        sb.append('}');
        return sb.toString();
    }

    protected static XdesBits[] bitmap(ByteBuffer byteBuffer) {
        // one extent contains 64 pages, each page reserve 2 state bits
        // one byte contains 8/2 = 4 pages' state bits
        byte[] bytes = new byte[16];
        XdesBits[] result = new XdesBits[64];
        byteBuffer.get(bytes);
        //        for (byte b : bytes) {
        //            System.err.printf("%X ", b);
        //        }
        //        System.err.println();
        for (int i = 0; i < 64; i++) {
            int nByte = i / 4;
            int nPageInByte = i % 4;
            int offset = (2 * nPageInByte);
            result[i] =
                    switch ((bytes[nByte] & (3 << offset)) >>> offset) {
                        case 0b00, 0b10 -> XdesBits.NONE;
                        case 0b01, 0b11 -> XdesBits.FREE;
                        default -> throw new IllegalStateException(
                                "Unexpected value: " + (bytes[nByte] & (3 << offset) >> offset));
                    };
        }
        return result;
    }
}
