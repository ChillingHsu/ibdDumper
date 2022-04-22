/* (C)2022 */
package parser;

import annotationProcessor.Sublayout;

public abstract class FlstNodeLayout extends Layout {
    @Sublayout protected FilAddrLayout<FlstNodeLayout> prev;
    @Sublayout protected FilAddrLayout<FlstNodeLayout> next;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FlstNode{");
        sb.append("prev=").append(prev);
        sb.append(", next=").append(next);
        sb.append('}');
        return sb.toString();
    }
}
