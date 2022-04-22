package parser;

import annotationProcessor.Length;

public abstract class PageNoLayout extends Layout {
    public static final long NULL = 0xffff_ffffL;

    @Length(4)
    protected Long raw;

    @Override
    public String toString() {
        return isNullPage() ? "NULL" : "#" + raw;
    }

    public boolean isNullPage() {
        return raw.equals(NULL);
    }
}
