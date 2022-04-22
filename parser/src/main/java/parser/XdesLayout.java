package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;

public abstract class XdesLayout extends Layout {
    // @Sublayout
    // protected FSPHeaderLayout fspHeader;
    @Length(112)
    protected byte[] zeros;

    @Sublayout(repeated = 256)
    protected XdesEntryLayout[] xdesEntries;

    @Length(5986)
    protected byte[] dumb;

    @Override
    public String toString() {
        return "Xdes Page";
    }
}
