package parser;

import annotationProcessor.Sublayout;
import formatter.Formatter;
import java.nio.ByteBuffer;
import java.util.List;
import parserGen.PageData;

public abstract class IndexLayout extends Layout {
    public IndexHeaderLayout getIndexHeader() {
        return indexHeader;
    }

    public FSEGHeaderLayout getLeaf() {
        return leaf;
    }

    public FSEGHeaderLayout getInternal() {
        return internal;
    }

    public PageDataLayout getPageData() {
        return pageData;
    }

    /** Transaction id: 6 bytes */
    public static final int DATA_TRX_ID = 1;

    /** Transaction ID type size in bytes. */
    public static final int DATA_TRX_ID_LEN = 6;

    /** Rollback data pointer: 7 bytes */
    public static final int DATA_ROLL_PTR = 2;

    /** Rollback data pointer type size in bytes. */
    public static final int DATA_ROLL_PTR_LEN = 7;

    public static final int SDI_DATA_ID_LEN = 8;
    public static final int SDI_DATA_TYPE_LEN = 4;
    public static final int SDI_DATA_UNCOMP_LEN = 4;
    public static final int SDI_DATA_COMP_LEN = 4;

    public static final int SDI_OFF_DATA_TYPE = 0;
    public static final int SDI_OFF_DATA_ID = SDI_OFF_DATA_TYPE + SDI_DATA_TYPE_LEN;
    public static final int SDI_OFF_DATA_TRX_ID = SDI_OFF_DATA_ID + SDI_DATA_ID_LEN;
    public static final int SDI_OFF_DATA_ROLL_PTR = SDI_OFF_DATA_TRX_ID + DATA_TRX_ID_LEN;
    public static final int REC_OFF_DATA_UNCOMP_LEN = SDI_OFF_DATA_ROLL_PTR + DATA_ROLL_PTR_LEN;
    public static final int REC_OFF_DATA_COMP_LEN = REC_OFF_DATA_UNCOMP_LEN + SDI_DATA_UNCOMP_LEN;
    public static final int REC_OFF_DATA_VARCHAR = REC_OFF_DATA_COMP_LEN + SDI_DATA_COMP_LEN;

    public static final List<RecordDirectory> SDI_RECORD_DIRECTORIES =
            List.of(
                    RecordDirectory.FixedColumn(
                            "TYPE",
                            false,
                            IndexLayout.SDI_DATA_TYPE_LEN,
                            Integer.class,
                            RecordDirectory.IntegerConverter),
                    RecordDirectory.FixedColumn(
                            "ID",
                            false,
                            IndexLayout.SDI_DATA_ID_LEN,
                            Long.class,
                            RecordDirectory.LongConverter),
                    RecordDirectory.FixedColumn(
                            "TRX_ID",
                            false,
                            IndexLayout.DATA_TRX_ID_LEN,
                            Long.class,
                            RecordDirectory.LongConverter),
                    RecordDirectory.FixedColumn(
                            "ROLL_PTR",
                            false,
                            IndexLayout.DATA_ROLL_PTR_LEN,
                            Long.class,
                            RecordDirectory.LongConverter),
                    RecordDirectory.FixedColumn(
                            "UNCOMP",
                            false,
                            IndexLayout.SDI_DATA_UNCOMP_LEN,
                            Integer.class,
                            RecordDirectory.IntegerConverter),
                    RecordDirectory.FixedColumn(
                            "COMP",
                            false,
                            IndexLayout.SDI_DATA_COMP_LEN,
                            Integer.class,
                            RecordDirectory.IntegerConverter),
                    RecordDirectory.VariableColumn("LOAD", false, String.class, i -> i));

    // TODO: the content of this record directories is likely wrong.
    public static final List<RecordDirectory> SDI_INTERNAL_RECORD_DIRECTORIES =
            List.of(
                    RecordDirectory.FixedColumn(
                            "TYPE",
                            false,
                            IndexLayout.SDI_DATA_TYPE_LEN,
                            Integer.class,
                            RecordDirectory.IntegerConverter),
                    RecordDirectory.FixedColumn(
                            "ID",
                            false,
                            IndexLayout.SDI_DATA_ID_LEN,
                            Long.class,
                            RecordDirectory.LongConverter),
                    RecordDirectory.FixedColumn(
                            "PageNo",
                            false,
                            Univ.SIZEOF_UINT32,
                            Integer.class,
                            RecordDirectory.IntegerConverter));

    // total: 16338 bytes
    @Sublayout protected IndexHeaderLayout indexHeader; // 36
    @Sublayout protected FSEGHeaderLayout leaf; // 10
    @Sublayout protected FSEGHeaderLayout internal; // 10

    @Sublayout(length = 16282, args = "indexHeader")
    protected PageDataLayout pageData;

    protected static PageDataLayout pageData(IndexHeaderLayout indexHeader, ByteBuffer byteBuffer) {
        PageDataLayout layout = new PageData(byteBuffer);
        layout.parse(indexHeader.nDirectorySlots);
        return layout;
    }

    @Override
    public void format(Formatter formatter) {
        formatter.item("<Index>");
        formatter.subItem(indexHeader::format);

        formatter.subItem(
                f -> {
                    f.append("<Leaf FSEGHeader> ->  ");
                    f.derefItem(leaf.inodePage, InodeEntryLayout.class);
                });
        formatter.subItem(
                f -> {
                    f.append("<Internal FSEGHeader> -> ");
                    f.derefItem(internal.inodePage, InodeEntryLayout.class);
                });
        formatter.subItem(
                f -> {
                    f.item("<Data>");
                    pageData.format(f);
                });
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Index{");
        sb.append("\t\nindexHeader=").append(indexHeader);
        sb.append("\t\ninternal-seg=").append(internal);
        sb.append("\t\nleaf-seg=").append(leaf);
        sb.append("\t\ndata=").append(pageData);
        sb.append('}');
        return sb.toString();
    }
}
