package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Formatter;
import java.util.Arrays;

/**
 * FILE SEGMENT INODE: Segment inode which is created for each segment in a tablespace. NOTE: in
 * purge we assume that a segment having only one currently used page can be freed in a few steps,
 * so that the freeing cannot fill the file buffer with bufferfixed file pages.
 */
public abstract class InodeLayout extends Layout {
    @Sublayout protected FlstNodeLayout inodePageNode;

    @Sublayout(repeated = 85)
    protected InodeEntryLayout[] inodeEntries;

    @Length(6)
    protected byte[] dumb;

    @Override
    public void format(Formatter formatter) {
        inodePageNode.format(formatter);
        formatter.subItem(f -> Arrays.stream(inodeEntries).forEach(entry -> entry.format(f)));
    }
}
