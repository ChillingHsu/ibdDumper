/* (C)2022 */
package parser;

import java.nio.ByteBuffer;

public enum FilPageType {
    /** B-tree node */
    INDEX(17855),

    /** R-tree node */
    RTREE(17854),

    /** Tablespace SDI Index page */
    SDI(17853),

    /** This page type is unused. */
    TYPE_UNUSED(1),

    /** Undo log page */
    UNDO_LOG(2),

    /** Index node */
    INODE(3),

    /** Insert buffer free list */
    IBUF_FREE_LIST(4),

    /* File page types introduced in MySQL/InnoDB 5.1.7 */
    /** Freshly allocated page */
    TYPE_ALLOCATED(0),

    /** Insert buffer bitmap */
    IBUF_BITMAP(5),

    /** System page */
    TYPE_SYS(6),

    /** Transaction system data */
    TYPE_TRX_SYS(7),

    /** File space header */
    TYPE_FSP_HDR(8),

    /** Extent descriptor page */
    TYPE_XDES(9),

    /** Uncompressed BLOB page */
    TYPE_BLOB(10),

    /** First compressed BLOB page */
    TYPE_ZBLOB(11),

    /** Subsequent compressed BLOB page */
    TYPE_ZBLOB2(12),

    /** In old tablespaces, garbage in TYPE is replaced with this value when flushing pages. */
    TYPE_UNKNOWN(13),

    /** Compressed page */
    COMPRESSED(14),

    /** Encrypted page */
    ENCRYPTED(15),

    /** Compressed and Encrypted page */
    COMPRESSED_AND_ENCRYPTED(16),

    /** Encrypted R-tree page */
    ENCRYPTED_RTREE(17),

    /** Uncompressed SDI BLOB page */
    SDI_BLOB(18),

    /** Commpressed SDI BLOB page */
    SDI_ZBLOB(19),

    /** Legacy doublewrite buffer page. */
    TYPE_LEGACY_DBLWR(20),

    /** Rollback Segment Array page */
    TYPE_RSEG_ARRAY(21),

    /** Index pages of uncompressed LOB */
    TYPE_LOB_INDEX(22),

    /** Data pages of uncompressed LOB */
    TYPE_LOB_DATA(23),

    /** The first page of an uncompressed LOB */
    TYPE_LOB_FIRST(24),

    /** The first page of a compressed LOB */
    TYPE_ZLOB_FIRST(25),

    /** Data pages of compressed LOB */
    TYPE_ZLOB_DATA(26),

    /** Index pages of compressed LOB. This page contains an array of z_index_entry_t objects. */
    TYPE_ZLOB_INDEX(27),

    /** Fragment pages of compressed LOB. */
    TYPE_ZLOB_FRAG(28),

    /** Index pages of fragment pages (compressed LOB). */
    TYPE_ZLOB_FRAG_ENTRY(29);

    private final short code;

    FilPageType(int code) {
        this.code = (short) code;
    }

    public static FilPageType getByCode(final short code) {
        for (FilPageType type : FilPageType.values()) {
            if (type.code == code) return type;
        }
        return null;
    }

    public static FilPageType fromByteArray(final byte[] bytes) {
        return getByCode(ByteBuffer.wrap(bytes).getShort());
    }
}
