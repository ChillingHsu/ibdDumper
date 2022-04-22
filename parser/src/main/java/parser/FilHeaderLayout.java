/* (C)2022 */
package parser;

import annotationProcessor.Length;
import formatter.Formatter;
import java.util.Base64;

public abstract class FilHeaderLayout extends Layout {
    @Length(4)
    protected byte[] checksum;

    @Length(4)
    protected Integer offset;

    public Integer getOffset() {
        return offset;
    }

    public Integer getPreviousPage() {
        return previousPage;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public FilPageType getPageType() {
        return pageType;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    @Length(4)
    protected Integer previousPage;

    @Length(4)
    protected Integer nextPage;

    @Length(8)
    protected Long lsn;

    @Length(2)
    protected FilPageType pageType;

    @Length(8)
    protected Long flushLsn;

    @Length(4)
    protected Integer spaceId;

    @Override
    public void format(Formatter formatter) {
        final StringBuilder sb = new StringBuilder("<FilHeader> Page ");
        sb.append("#").append(offset);
        sb.append(", Type: ").append(pageType);
        sb.append(", Space ID: ").append(spaceId);
        sb.append(", LSN: ").append(lsn);
        if (pageType == FilPageType.INDEX) {
            sb.append(", Previous: ").append(previousPage);
            sb.append(", Next: ").append(nextPage);
        }
        if (spaceId == 0 && offset == 0) sb.append(", Flush LSN: ").append(flushLsn);
        sb.append(", Checksum: ")
                .append(Base64.getEncoder().withoutPadding().encodeToString(checksum));
        formatter.item(sb.toString());
    }
}
