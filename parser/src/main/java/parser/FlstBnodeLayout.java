/* (C)2022 */
package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;

public abstract class FlstBnodeLayout extends Layout {
    @Length(4)
    protected Long len; // unsigned

    @Sublayout protected FilAddrLayout<FlstNodeLayout> first;
    @Sublayout protected FilAddrLayout<FlstNodeLayout> last;

    @Override
    public String toString() {
        if (len == 0) return "nil";
        final StringBuffer sb = new StringBuffer("Flst{");
        sb.append("len=").append(len);
        sb.append(", first=").append(first);
        sb.append(", last=").append(last);
        sb.append('}');
        return sb.toString();
    }
}
