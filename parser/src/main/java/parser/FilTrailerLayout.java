/* (C)2022 */
package parser;

import annotationProcessor.Length;

public abstract class FilTrailerLayout extends Layout {
    @Length(4)
    protected Integer checksum;

    @Length(4)
    protected Integer lowLsn;
}
