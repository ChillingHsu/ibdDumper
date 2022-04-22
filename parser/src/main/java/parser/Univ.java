package parser;

// mysql-server/storage/innobase/include/univ.i
public abstract class Univ {
    public static final int SIZEOF_UINT32 = 4;
    public static final int SIZEOF_UINT64 = 8;
    public static final long UINT64_MAX = -1L;

    public static final int ZIP_SIZE_SHIFT_MAX = 14;
    public static final int ZIP_SIZE_SHIFT_MIN = 10;

    public static final int PAGE_SIZE_SHIFT_MIN = 12;
    public static final int PAGE_SIZE_SHIFT_MAX = 16;

    public static final int PAGE_SIZE_SHIFT = 14;
    public static final int PAGE_SIZE_SHIFT_ORIG = 14;
    public static final int PAGE_SIZE_ORIG = 1 << PAGE_SIZE_SHIFT_ORIG;
    public static final int PAGE_SIZE = 1 << PAGE_SIZE_SHIFT;
    public static final int PAGE_SSIZE_MAX = PAGE_SIZE_SHIFT - ZIP_SIZE_SHIFT_MIN + 1;
    public static final int PAGE_SSIZE_MIN = PAGE_SIZE_SHIFT_MIN - ZIP_SIZE_SHIFT_MIN + 1;

    public static final int LOB_BTR_EXTERN_SPACE_ID = 0;
    public static final int LOB_BTR_EXTERN_PAGE_NO = 4;
    public static final int LOB_BTR_EXTERN_OFFSET = 8;
    public static final int LOB_BTR_EXTERN_VERSION = LOB_BTR_EXTERN_OFFSET;
    public static final int LOB_BTR_EXTERN_LEN = 12;
}
