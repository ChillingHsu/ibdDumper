/* (C)2022 */
package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Formatter;

/* ref: storage/innobase/include/fsp0fsp.h */
public abstract class FSPHeaderLayout extends Layout {
    /**
     * They should be 0 for ROW_FORMAT=COMPACT and ROW_FORMAT=REDUNDANT. The newer row formats,
     * COMPRESSED and DYNAMIC, will have at least the DICT_TF_COMPACT bit set.
     */
    @Length(4)
    protected Integer spaceId;

    @Length(4)
    protected byte[] notUsed;

    @Length(4)
    protected Integer size; /* Current size of the space in pages */

    /**
     * Minimum page number for which the free list has not been initialized: the pages >= this limit
     * are, by definition, free; note that in a single-table tablespace where size < 64 pages, this
     * number is 64, i.e., we have initialized the space about the first extent, but have not
     * physically allocated those pages to the file
     */
    @Length(4)
    protected Integer freeLimit;

    @Sublayout protected FSPFlagsLayout spaceFlags;

    /** number of used pages in the FSP_FREE_FRAG list. */
    @Length(4)
    protected Integer fragNUsed;

    /* list of free extents */
    @Sublayout protected FlstBnodeLayout free;

    /* list of partially free extents not belonging to any segment */
    @Sublayout protected FlstBnodeLayout freeFrag;

    /* list of full extents not belonging to any segment */
    @Sublayout protected FlstBnodeLayout fullFrag;

    /* 8 bytes which give the first unused segment id */
    @Length(8)
    protected Long nextSegId;

    /** list of pages containing segment headers, where all the segment inode slots are reserved */
    @Sublayout protected FlstBnodeLayout segInodesFull;

    /**
     * list of pages containing segment headers, where not all the segment header slots are reserved
     */
    @Sublayout protected FlstBnodeLayout segInodesFree;

    public void format(Formatter formatter) {
        formatter.item("<FSP Header>");
        formatter.subItem(
                f -> {
                    f.item("Space ID: " + this.spaceId);
                    f.item("Highest number of page in file: " + this.size);
                    f.item("Highest number of page initialized: " + this.freeLimit);
                    f.item("Flags: " + this.spaceFlags);
                    f.item("FREE: " + this.free);
                    f.item("FREE_FRAG (" + this.fragNUsed + " page(s) used): " + this.freeFrag);
                    f.item("FULL_FRAG: " + this.fullFrag);
                    f.item("FULL_INODES: " + this.segInodesFull);
                    f.item("FREE_INODES: " + this.segInodesFree);
                    f.item("Next unused segment ID: " + this.nextSegId);
                });
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FSPHeaderLayout{");
        sb.append("spaceId=").append(spaceId);
        sb.append(", size=").append(size);
        sb.append(", freeLimit=").append(freeLimit);
        sb.append(", spaceFlags=").append(spaceFlags);
        sb.append(", fragNUsed=").append(fragNUsed);
        sb.append(", free=").append(free);
        sb.append(", freeFrag=").append(freeFrag);
        sb.append(", fullFrag=").append(fullFrag);
        sb.append(", nextSegId=").append(nextSegId);
        sb.append(", segInodesFull=").append(segInodesFull);
        sb.append(", segInodesFree=").append(segInodesFree);
        sb.append('}');
        return sb.toString();
    }

    public FSPFlagsLayout getSpaceFlags() {
        return spaceFlags;
    }
}
