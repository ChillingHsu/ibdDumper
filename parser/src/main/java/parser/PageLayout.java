/* (C)2022 */
package parser;

import annotationProcessor.Sublayout;
import formatter.Formatter;
import java.nio.ByteBuffer;
import java.util.Base64;
import parserGen.*;

public abstract class PageLayout extends Layout {
    public FilHeaderLayout getFilHeader() {
        return filHeader;
    }

    @Sublayout protected FilHeaderLayout filHeader;

    @Sublayout(length = 16338, args = "filHeader")
    protected Layout content;

    @Sublayout protected FilTrailerLayout filTrailer;

    @Override
    public void format(Formatter formatter) {
        formatter.subItem(filHeader::format);
        formatter.subItem(content::format);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Page{");
        sb.append("Page Offset: ").append(filHeader.offset);
        sb.append(", Page Type: ").append(filHeader.pageType);
        sb.append(", Page Space ID: ").append(filHeader.spaceId);
        sb.append(", LSN: ").append(filHeader.lsn);
        if (filHeader.pageType == FilPageType.INDEX) {
            sb.append(", Previous Page: ").append(filHeader.previousPage);
            sb.append(", Next Page: ").append(filHeader.nextPage);
        }
        if (filHeader.spaceId == 0 && filHeader.offset == 0)
            sb.append(", Flush LSN: ").append(filHeader.flushLsn);
        sb.append(", Checksum: ")
                .append(Base64.getEncoder().withoutPadding().encodeToString(filHeader.checksum));
        sb.append('}');
        return sb.toString();
    }

    protected static Layout content(FilHeaderLayout filHeader, ByteBuffer byteBuffer) {
        if (filHeader.spaceId == 0)
            throw new UnsupportedOperationException("Cannot resolve System Table Space File");
        return switch (filHeader.pageType) {
            case TYPE_FSP_HDR -> new FSPHDR(byteBuffer);
            case INODE -> new Inode(byteBuffer);
            case TYPE_XDES -> new Xdes(byteBuffer);
            case INDEX, SDI -> new Index(byteBuffer);
            default -> new UnknownPageContent(byteBuffer);
        };
    }

    public <T extends Layout> T getContent() {
        return (T) content;
    }
}
