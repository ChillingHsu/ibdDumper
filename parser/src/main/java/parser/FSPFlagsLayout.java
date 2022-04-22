package parser;

import annotationProcessor.Length;

/** storage/innobase/include/fsp0types.h */
public abstract class FSPFlagsLayout extends Layout {
    /** Width of the POST_ANTELOPE flag */
    protected static final int WIDTH_POST_ANTELOPE = 1;
    /** Number of flag bits used to indicate the tablespace zip page size */
    protected static final int WIDTH_ZIP_SSIZE = 4;
    /**
     * Width of the ATOMIC_BLOBS flag. The ability to break up a long column into an in-record
     * prefix and an externally stored part is available to ROW_FORMAT=REDUNDANT and
     * ROW_FORMAT=COMPACT.
     */
    protected static final int WIDTH_ATOMIC_BLOBS = 1;
    /** Number of flag bits used to indicate the tablespace page size */
    protected static final int WIDTH_PAGE_SSIZE = 4;
    /**
     * Width of the DATA_DIR flag. This flag indicates that the tablespace is found in a remote
     * location, not the default data directory.
     */
    protected static final int WIDTH_DATA_DIR = 1;
    /**
     * Width of the SHARED flag. This flag indicates that the tablespace was created with CREATE
     * TABLESPACE and can be shared by multiple tables.
     */
    protected static final int WIDTH_SHARED = 1;
    /**
     * Width of the TEMPORARY flag. This flag indicates that the tablespace is a temporary
     * tablespace and everything in it is temporary, meaning that it is for a single client and
     * should be deleted upon startup if it exists.
     */
    protected static final int WIDTH_TEMPORARY = 1;
    /**
     * Width of the encryption flag. This flag indicates that the tablespace is a tablespace with
     * encryption.
     */
    protected static final int WIDTH_ENCRYPTION = 1;
    /** Width of the SDI flag. This flag indicates the presence of tablespace dictionary. */
    protected static final int WIDTH_SDI = 1;

    /** Width of all the currently known tablespace flags */
    protected static final int WIDTH =
            (WIDTH_POST_ANTELOPE
                    + WIDTH_ZIP_SSIZE
                    + WIDTH_ATOMIC_BLOBS
                    + WIDTH_PAGE_SSIZE
                    + WIDTH_DATA_DIR
                    + WIDTH_SHARED
                    + WIDTH_TEMPORARY
                    + WIDTH_ENCRYPTION
                    + WIDTH_SDI);

    /** A mask of all the known/used bits in tablespace flags */
    protected static final int MASK = (~(~0 << WIDTH));

    /** Zero relative shift position of the POST_ANTELOPE field */
    protected static final int POS_POST_ANTELOPE = 0;
    /** Zero relative shift position of the ZIP_SSIZE field */
    protected static final int POS_ZIP_SSIZE = (POS_POST_ANTELOPE + WIDTH_POST_ANTELOPE);
    /** Zero relative shift position of the ATOMIC_BLOBS field */
    protected static final int POS_ATOMIC_BLOBS = (POS_ZIP_SSIZE + WIDTH_ZIP_SSIZE);
    /** Zero relative shift position of the PAGE_SSIZE field */
    protected static final int POS_PAGE_SSIZE = (POS_ATOMIC_BLOBS + WIDTH_ATOMIC_BLOBS);
    /** Zero relative shift position of the start of the DATA_DIR bit */
    protected static final int POS_DATA_DIR = (POS_PAGE_SSIZE + WIDTH_PAGE_SSIZE);
    /** Zero relative shift position of the start of the SHARED bit */
    protected static final int POS_SHARED = (POS_DATA_DIR + WIDTH_DATA_DIR);
    /** Zero relative shift position of the start of the TEMPORARY bit */
    protected static final int POS_TEMPORARY = (POS_SHARED + WIDTH_SHARED);
    /** Zero relative shift position of the start of the ENCRYPTION bit */
    protected static final int POS_ENCRYPTION = (POS_TEMPORARY + WIDTH_TEMPORARY);
    /** Zero relative shift position of the start of the SDI bits */
    protected static final int POS_SDI = (POS_ENCRYPTION + WIDTH_ENCRYPTION);

    /** Zero relative shift position of the start of the UNUSED bits */
    protected static final int POS_UNUSED = (POS_SDI + WIDTH_SDI);

    /** Bit mask of the POST_ANTELOPE field */
    protected static final int MASK_POST_ANTELOPE =
            ((~(~0 << WIDTH_POST_ANTELOPE)) << POS_POST_ANTELOPE);
    /** Bit mask of the ZIP_SSIZE field */
    protected static final int MASK_ZIP_SSIZE = ((~(~0 << WIDTH_ZIP_SSIZE)) << POS_ZIP_SSIZE);
    /** Bit mask of the ATOMIC_BLOBS field */
    protected static final int MASK_ATOMIC_BLOBS =
            ((~(~0 << WIDTH_ATOMIC_BLOBS)) << POS_ATOMIC_BLOBS);
    /** Bit mask of the PAGE_SSIZE field */
    protected static final int MASK_PAGE_SSIZE = ((~(~0 << WIDTH_PAGE_SSIZE)) << POS_PAGE_SSIZE);
    /** Bit mask of the DATA_DIR field */
    protected static final int MASK_DATA_DIR = ((~(~0 << WIDTH_DATA_DIR)) << POS_DATA_DIR);
    /** Bit mask of the SHARED field */
    protected static final int MASK_SHARED = ((~(~0 << WIDTH_SHARED)) << POS_SHARED);
    /** Bit mask of the TEMPORARY field */
    protected static final int MASK_TEMPORARY = ((~(~0 << WIDTH_TEMPORARY)) << POS_TEMPORARY);
    /** Bit mask of the ENCRYPTION field */
    protected static final int MASK_ENCRYPTION = ((~(~0 << WIDTH_ENCRYPTION)) << POS_ENCRYPTION);
    /** Bit mask of the SDI field */
    protected static final int MASK_SDI = ((~(~0 << WIDTH_SDI)) << POS_SDI);

    /** Return the value of the POST_ANTELOPE field */
    public boolean isPostAntelope() {
        return ((flags & MASK_POST_ANTELOPE) >>> POS_POST_ANTELOPE) != 0;
    }

    /** Return the value of the ZIP_SSIZE field */
    public int getZipSsize() {
        return (flags & MASK_ZIP_SSIZE) >>> POS_ZIP_SSIZE;
    }

    /** Return the value of the ATOMIC_BLOBS field */
    public boolean hasAtomicBlobs() {
        return (flags & MASK_ATOMIC_BLOBS) >>> POS_ATOMIC_BLOBS != 0;
    }

    /** Return the value of the PAGE_SSIZE field */
    public int getPageSsize() {
        return (flags & MASK_PAGE_SSIZE) >>> POS_PAGE_SSIZE;
    }

    /** Return the value of the DATA_DIR field */
    public boolean hasDataDir() {
        return (flags & MASK_DATA_DIR) >>> POS_DATA_DIR != 0;
    }

    /** Return the contents of the SHARED field */
    public boolean isShared() {
        return (flags & MASK_SHARED) >>> POS_SHARED != 0;
    }

    /** Return the contents of the TEMPORARY field */
    public boolean isTemporary() {
        return (flags & MASK_TEMPORARY) >>> POS_TEMPORARY != 0;
    }

    /** Return the contents of the ENCRYPTION field */
    public boolean isEncryption() {
        return (flags & MASK_ENCRYPTION) >>> POS_ENCRYPTION != 0;
    }

    /** Return the value of the SDI field */
    public boolean hasSdi() {
        return (flags & MASK_SDI) >>> POS_SDI != 0;
    }

    /** Return the contents of the UNUSED bits */
    public int getUnused() {
        return flags >>> POS_UNUSED;
    }

    /** Return true if flags are not set */
    public boolean notSet() {
        return (flags & MASK) == 0;
    }

    @Length(4)
    protected Integer flags;

    // inline bool fsp_flags_is_valid(uint32_t flags)
    public boolean validate() {
        /* The Antelope row formats REDUNDANT and COMPACT did
         * not use tablespace flags, so the entire 4-byte field
         * is zero for Antelope row formats. */
        if (flags == 0) return true;

        /* Row_FORMAT=COMPRESSED and ROW_FORMAT=DYNAMIC use a feature called
         * ATOMIC_BLOBS which builds on the page structure introduced for the
         * COMPACT row format by allowing long fields to be broken into prefix
         * and externally stored parts. So if it is Post_antelope, it uses
         * Atomic BLOBs. */
        if (!(isPostAntelope() && hasAtomicBlobs())) return false;

        if (getUnused() != 0) return false;

        final int PAGE_ZIP_SSIZE_MAX = (Univ.ZIP_SIZE_SHIFT_MAX - Univ.ZIP_SIZE_SHIFT_MIN + 1);
        /* The zip ssize can be zero if it is other than compressed row format,
         * or it could be from 1 to the max. */
        if (getZipSsize() > PAGE_ZIP_SSIZE_MAX) return false;

        /* The actual page size must be within 4k and 64K (3 =< ssize =< 7). */
        if (getPageSsize() != 0
                && (getPageSsize() < Univ.PAGE_SSIZE_MIN || getPageSsize() > Univ.PAGE_SSIZE_MAX))
            return false;

        /* Only single-table tablespaces use the DATA DIRECTORY clause.
         * It is not compatible with the TABLESPACE clause.  Nor is it
         * compatible with the TEMPORARY clause. */
        if (hasDataDir() && (isShared() || isTemporary())) return false;

        /* Only single-table and general tablespaces and not temp tablespaces
         * use the encryption clause. */
        if (isEncryption() && isTemporary()) return false;

        return true;
    }

    @Override
    public String toString() {
        boolean postAntelope = isPostAntelope();
        int zipSsize = getZipSsize();
        boolean atomicBlobs = hasAtomicBlobs();
        int pageSsize = getPageSsize();
        boolean hasDataDir = hasDataDir();
        boolean isShared = isShared();
        boolean isTemp = isTemporary();
        boolean isEncrytion = isEncryption();
        boolean hasSdi = hasSdi();
        int unused = getUnused();
        final StringBuffer sb = new StringBuffer("FSPFlags{");
        if (hasSdi) sb.append("SDI ");
        if (postAntelope) sb.append("Post-antelope ");
        if (atomicBlobs) sb.append("Atomic-blobs ");
        if (zipSsize != 0) sb.append("Zip-page-size ").append(1 << zipSsize).append("KB ");
        if (pageSsize != 0) sb.append("Page-size ").append((1 << pageSsize) / 2.0).append("KB ");
        if (hasDataDir) sb.append("Data-directory ");
        if (isShared) sb.append("Shared ");
        if (isTemp) sb.append("Temp ");
        if (isEncrytion) sb.append("Encrypted ");
        sb.deleteCharAt(sb.lastIndexOf(" "));
        sb.append('}');
        return sb.toString();
    }
}
