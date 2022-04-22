package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;

public abstract class FSEGHeaderLayout extends Layout {
    @Length(4)
    protected Integer spaceId;

    @Sublayout protected FilAddrLayout<InodeEntryLayout> inodePage;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FSEGH{");
        sb.append(inodePage);
        sb.append('}');
        return sb.toString();
    }
}
