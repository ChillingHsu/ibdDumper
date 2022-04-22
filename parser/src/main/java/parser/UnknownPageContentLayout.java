/* (C)2022 */
package parser;

import annotationProcessor.Length;

public abstract class UnknownPageContentLayout extends Layout {
    @Length(16338)
    protected byte[] content;

    @Override
    public String toString() {
        return "UnknownPageContent";
    }
}
