package parser;

import annotationProcessor.Length;
import formatter.Formatter;

/* storage/innobase/include/page0types.h */
public abstract class IndexHeaderLayout extends Layout {

    @Length(2)
    protected Integer nDirectorySlots;

    @Length(2)
    protected Integer heapTopOffset;

    @Length(2)
    protected Integer nHeapRecordsAndFormat;

    @Length(2)
    protected Integer freeRecordListOffset;

    @Length(2)
    protected Integer garbageSize;

    @Length(2)
    protected Integer lastInsertedRecordOffset;

    @Length(2)
    protected InsertionDirection lastInsertDirection;

    @Length(2)
    protected Integer nDirection;

    @Length(2)
    protected Integer nUserRecords;

    @Length(8)
    protected Long maxTransactionId;

    @Length(2)
    protected Integer level;

    @Length(8)
    protected Long indexId;

    @Override
    public void format(Formatter f) {
        f.item(String.format("Lvl. %d of Idx. %d", level, indexId));
        f.item("Format: " + ((nHeapRecordsAndFormat & 0x8000) != 0 ? "COMPACT" : "REDUNDANT"));
        f.item("Max Trx ID: " + maxTransactionId);
        f.item(
                String.format(
                        "Record Number / Heap Record Number: %d / %d",
                        nUserRecords, (nHeapRecordsAndFormat & ~0x8000)));
        f.item(
                String.format(
                        "Garbage Space: %d Byte(s) starts @%d", garbageSize, freeRecordListOffset));
        f.item("Last insertion @" + lastInsertedRecordOffset);
        f.item(
                String.format(
                        "Last %d insertion(s) in direction %s", nDirection, lastInsertDirection));
        f.item("Heap Top @" + heapTopOffset);
        f.item("Number of Directory Slot (2 Bytes each): " + nDirectorySlots);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IndexHeaderLayout{");
        sb.append("nDirectorySlots=").append(nDirectorySlots);
        sb.append(", heapTopOffset=").append(heapTopOffset);
        sb.append(", nHeapRecords=").append(nHeapRecordsAndFormat & ~0x8000);
        sb.append(", rowFormat=")
                .append((nHeapRecordsAndFormat & 0x8000) != 0 ? "COMPACT" : "REDUNDANT");
        sb.append(", freeRecordListOffset=").append(freeRecordListOffset);
        sb.append(", garbageSize=").append(garbageSize);
        sb.append(", lastInsertedRecordOffset=").append(lastInsertedRecordOffset);
        sb.append(", lastInsertDirection=").append(lastInsertDirection);
        sb.append(", nDirection=").append(nDirection);
        sb.append(", nUserRecords=").append(nUserRecords);
        sb.append(", maxTransactionId=").append(maxTransactionId);
        sb.append(", level=").append(level);
        sb.append(", indexId=").append(indexId);
        sb.append('}');
        return sb.toString();
    }

    public Integer getnDirectorySlots() {
        return nDirectorySlots;
    }

    public Integer getHeapTopOffset() {
        return heapTopOffset;
    }

    public Integer getnHeapRecordsAndFormat() {
        return nHeapRecordsAndFormat;
    }

    public Integer getFreeRecordListOffset() {
        return freeRecordListOffset;
    }

    public Integer getGarbageSize() {
        return garbageSize;
    }

    public Integer getLastInsertedRecordOffset() {
        return lastInsertedRecordOffset;
    }

    public InsertionDirection getLastInsertDirection() {
        return lastInsertDirection;
    }

    public Integer getnDirection() {
        return nDirection;
    }

    public Integer getnUserRecords() {
        return nUserRecords;
    }

    public Long getMaxTransactionId() {
        return maxTransactionId;
    }

    public Integer getLevel() {
        return level;
    }

    public Long getIndexId() {
        return indexId;
    }
}
