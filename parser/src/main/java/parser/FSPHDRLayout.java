/* (C)2022 */
package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Formatter;

public abstract class FSPHDRLayout extends Layout {
    static final int SDI_VERSION_LEN = 4;
    static final int SDI_PAGE_NUM_LEN = 4;
    static final int SDI_HEADER_LEN = SDI_VERSION_LEN + SDI_PAGE_NUM_LEN;
    // os0enc.h
    static final int ENC_MAGIC_SIZE = 3;

    static final int ENC_KEY_LEN = 32;
    static final int ENC_SERVER_UUID_LEN = 36;
    /**
     * Encryption information total size: magic number + master_key_id + key + iv + server_uuid +
     * checksum
     */
    static final int ENC_INFO_SIZE =
            ENC_MAGIC_SIZE
                    + Univ.SIZEOF_UINT32
                    + ENC_KEY_LEN * 2
                    + ENC_SERVER_UUID_LEN
                    + Univ.SIZEOF_UINT32;
    /** Maximum size of Encryption information considering all formats v1, v2 & v3. */
    static final int ENC_INFO_MAX_SIZE = ENC_INFO_SIZE + Univ.SIZEOF_UINT32;

    public FSPHeaderLayout getFspHeader() {
        return fspHeader;
    }

    public XdesEntryLayout[] getXdesEntries() {
        return xdesEntries;
    }

    @Sublayout protected FSPHeaderLayout fspHeader;

    @Sublayout(repeated = 256)
    protected XdesEntryLayout[] xdesEntries;

    @Length(ENC_INFO_MAX_SIZE)
    protected byte[] encrypt_info_dumb;

    @Length(SDI_VERSION_LEN)
    protected Integer sdiVersion;

    public Integer getSdiPageNo() {
        return sdiPageNo;
    }

    @Length(SDI_PAGE_NUM_LEN)
    protected Integer sdiPageNo;

    @Length(5986 - ENC_INFO_MAX_SIZE - SDI_HEADER_LEN)
    protected byte[] dumb;
    // 5986
    @Override
    public void format(Formatter formatter) {
        fspHeader.format(formatter);
        formatter.item("<XdesEntries>");
        formatter.subItem(
                f -> {
                    for (XdesEntryLayout entry : xdesEntries) {
                        entry.format(f);
                    }
                    f.item("SDI Page No." + sdiPageNo + "(Ver. " + sdiVersion + ")");
                });
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FSPHDRLayout{");
        sb.append("fspHeader=").append(fspHeader);
        sb.append('}');
        return sb.toString();
    }
}
