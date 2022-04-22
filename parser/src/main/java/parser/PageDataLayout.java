package parser;

import annotationProcessor.Length;
import annotationProcessor.Sublayout;
import formatter.Formatter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Consumer;
import parserGen.FSEGHeader;
import parserGen.FilHeader;
import parserGen.IndexHeader;
import parserGen.SystemRecord;

public abstract class PageDataLayout extends Layout {
    // 16282 Bytes
    private static final int BASE_ADDR =
            FilHeader.layoutSize + IndexHeader.layoutSize + 2 * FSEGHeader.layoutSize;
    public static final int RECORD_HEAD_LENGTH = 5;
    private static final int INFIMUM_ORIGIN = BASE_ADDR + RECORD_HEAD_LENGTH;
    private static final int SUPREMUM_ORIGIN =
            BASE_ADDR + SystemRecord.layoutSize + RECORD_HEAD_LENGTH;
    private static final int USER_RECORD_ORIGIN_OFFSET = -(BASE_ADDR + 2 * SystemRecord.layoutSize);

    @Sublayout protected SystemRecordLayout infimum;
    @Sublayout protected SystemRecordLayout supremum;

    @Length(16282 - 26)
    protected byte[] userRecordsAndPageDirectory;

    protected List<RecordDirectory> recordDirectories;
    public SortedMap<Short, DataRecord> dataRecords = new TreeMap<>();
    public short[] pageDirectories;

    @Override
    public void format(Formatter f) {
        f.item("<Records>");
        f.subItem(
                subF -> {
                    infimum.format(subF);
                    supremum.format(subF);
                    for (DataRecord record : dataRecords.values()) {
                        subF.item(record.toString());
                    }
                });
        f.item("<Page Directory>");
        f.subItem(
                subF -> {
                    for (short directory : pageDirectories) {
                        if (dataRecords.get(directory) != null) {
                            subF.item(
                                    "@"
                                            + String.valueOf(directory)
                                            + " -> REC ORD #"
                                            + dataRecords.get(directory).Order());
                        } else {
                            subF.item("@" + String.valueOf(directory) + " -> ?");
                        }
                    }
                });
    }

    public void parse(int nDirectorySlots) {
        int currentOrigin = INFIMUM_ORIGIN + infimum.nextRecordOffset;
        for (; ; ) {
            DataRecord dataRecord = makeRecord(currentOrigin);
            dataRecords.put((short) currentOrigin, dataRecord);
            currentOrigin += dataRecord.next();
            if (currentOrigin == SUPREMUM_ORIGIN) break;
            if (currentOrigin < 0) throw new RuntimeException("Corrupted record chain encountered");
        }

        pageDirectories = new short[nDirectorySlots];
        ByteBuffer dirSlotBuffer =
                ByteBuffer.wrap(
                        userRecordsAndPageDirectory,
                        userRecordsAndPageDirectory.length - 2 * nDirectorySlots,
                        2 * nDirectorySlots);
        for (int i = nDirectorySlots - 1; i >= 0; --i) {
            pageDirectories[i] = dirSlotBuffer.getShort();
        }
    }

    private DataRecord makeRecord(int origin) {
        origin += USER_RECORD_ORIGIN_OFFSET;
        if (origin < 0) throw new RuntimeException("Negative offset");
        byte[] rawRecords = userRecordsAndPageDirectory;
        int head = origin - RECORD_HEAD_LENGTH;
        short nextOriginOffset =
                (short) (((rawRecords[head + 3] << 8) & 0xFF00) + (rawRecords[head + 4] & 0xFF));
        return new DataRecord(
                RecordInfo.getByCode(rawRecords[head]),
                (short) (rawRecords[head] & 0xF),
                (short) (((((short) rawRecords[head + 1]) << 8) ^ (rawRecords[head + 2])) >> 3),
                RecordType.getByCode((short) (rawRecords[head + 2] & 0x7)),
                nextOriginOffset);
    }

    public void setRecordDirectories(List<RecordDirectory> recordDirectories) {
        if (this.recordDirectories != null) return;
        this.recordDirectories = Collections.unmodifiableList(recordDirectories);
        int nNullable = 0;
        int nVariable = 0;
        int fixedLength = 0;
        for (RecordDirectory rd : recordDirectories) {
            if (rd.nullable) ++nNullable;
            if (rd.variable) ++nVariable;
            if (!rd.variable) {
                if (rd.length < 0)
                    throw new IllegalStateException("Negative length of one record column");
                fixedLength += rd.length;
            }
        }
        fetchRecordData(nNullable, nVariable, fixedLength);
    }

    private void fetchRecordData(int nNullable, int nVariable, int fixedLength) {
        final int BITS_IN_BYTE = 8;
        int nullableBytes = (nNullable + BITS_IN_BYTE - 1) / BITS_IN_BYTE; // ceiling of div 8
        for (Map.Entry<Short, DataRecord> entry : dataRecords.entrySet()) {
            int origin = entry.getKey() + USER_RECORD_ORIGIN_OFFSET;
            DataRecord rec = entry.getValue();
            int offset = -RECORD_HEAD_LENGTH;
            if (nullableBytes > 0) {
                offset -= nullableBytes;
                byte[] nullBytes =
                        Arrays.copyOfRange(
                                userRecordsAndPageDirectory,
                                origin + offset,
                                origin + offset + nullableBytes);

                for (int i = 0; i < nNullable; ++i) {
                    rec.nulls[i] =
                            1
                                    == ((nullBytes[i / BITS_IN_BYTE]
                                                    >> (BITS_IN_BYTE - 1 - (i % BITS_IN_BYTE)))
                                            & 1);
                }
            }
            int totalDataInPageLength = fixedLength;
            rec.variableColumnLengths = new int[nVariable];
            for (int i = 0; i < nVariable; ++i) {
                offset--;
                int possiblePartialLength = 0xff & userRecordsAndPageDirectory[origin + offset];
                int dataInPageLength;
                if ((possiblePartialLength & 0x80) != 0) {
                    dataInPageLength = (possiblePartialLength & 0x3f) << BITS_IN_BYTE;
                    if ((possiblePartialLength & 0x40) != 0) {
                        // Rec is stored externally with 768 byte prefix inline
                        rec.externalData = true;
                        dataInPageLength += Univ.LOB_BTR_EXTERN_LEN;
                        throw new UnsupportedOperationException(
                                "External row data is currently unsupported");
                    } else {
                        dataInPageLength += 0xff & userRecordsAndPageDirectory[origin + (--offset)];
                    }
                } else {
                    dataInPageLength = possiblePartialLength;
                }
                totalDataInPageLength += dataInPageLength;
                rec.variableColumnLengths[i] = dataInPageLength;
            }

            rec.rawData =
                    Arrays.copyOfRange(
                            userRecordsAndPageDirectory, origin, origin + totalDataInPageLength);
        }
    }

    public static Map<String, Object> parseRawData(
            DataRecord record,
            List<RecordDirectory> recordDirectories,
            Consumer<Map<String, Object>> postProcessor) {
        HashMap<String, Object> map = new HashMap<>();
        int varColCounter = 0;
        int nullableCounter = 0;
        int offset = 0;
        for (RecordDirectory rd : recordDirectories) {
            if (rd.nullable) {
                ++nullableCounter;
                if (record.nulls[nullableCounter - 1]) {
                    map.put(rd.name, rd.converter.apply(null));
                    continue;
                }
            }
            if (rd.variable) {
                ++varColCounter;
                int nextOffset = offset + record.variableColumnLengths[varColCounter - 1];
                map.put(
                        rd.name,
                        rd.converter.apply(Arrays.copyOfRange(record.rawData, offset, nextOffset)));
                offset = nextOffset;
                continue;
            }
            if (rd.length < 0) throw new IllegalStateException("Negative column length");
            int nextOffset = offset + rd.length;
            map.put(
                    rd.name,
                    rd.converter.apply(Arrays.copyOfRange(record.rawData, offset, nextOffset)));
            offset = nextOffset;
        }
        if (postProcessor != null) postProcessor.accept(map);
        return map;
    }
}
