/* (C)2022 */
package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Referable;

public abstract class FilAddrLayout<R> extends Layout implements Referable<PageLayout[], R> {
    @Sublayout protected PageNoLayout pageNo;

    @Length(2)
    protected Short offset;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("&{");
        sb.append(pageNo);
        sb.append("+").append(offset);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public R deref(PageLayout[] context, final Class<R> targetClass) {
        if (pageNo.isNullPage() || targetClass == null) return null;
        return context[Math.toIntExact(pageNo.raw)].offsetAt(offset, targetClass, false);
    }
}
