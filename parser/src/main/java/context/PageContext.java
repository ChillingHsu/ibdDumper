package context;

import static parser.IndexLayout.SDI_INTERNAL_RECORD_DIRECTORIES;
import static parser.IndexLayout.SDI_RECORD_DIRECTORIES;

import formatter.Formatter;
import formatter.Referable;
import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.stream.Stream;
import parser.*;

public class PageContext implements Formatter {
    Formatter internalFormatter;
    PageLayout[] pages;

    public PageContext(Formatter internalFormatter, PageLayout[] pages) {
        this.internalFormatter = internalFormatter;
        this.pages = pages;
    }

    @Override
    public void child() {
        internalFormatter.child();
    }

    @Override
    public void parent() {
        internalFormatter.parent();
    }

    @Override
    public void item(String content) {
        internalFormatter.item(content);
    }

    @Override
    public void append(String content) {
        internalFormatter.append(content);
    }

    @Override
    public void end() {
        internalFormatter.end();
    }

    @Override
    public <T extends Layout & Referable<?, R>, R extends Layout> R deref(
            T ref, Class<R> targetClass) {
        return ((Referable<PageLayout[], R>) ref).deref(pages, targetClass);
    }

    @Override
    public String toString() {
        return internalFormatter.toString();
    }

    public int getSdiPageNo() {
        FSPHDRLayout fspHdr = pages[0].getContent();
        FSPFlagsLayout spaceFlags = fspHdr.getFspHeader().getSpaceFlags();
        // TODO: new more accurate exception class
        if (!spaceFlags.validate()) throw new IllegalStateException("Invalid table space flags");
        if (!spaceFlags.hasSdi()) throw new IllegalStateException("No SDI page found");
        return fspHdr.getSdiPageNo();
    }

    public Stream<DataRecord> traverseSdiDataRecords() {
        FilPageType pageType = pages[getSdiPageNo()].getFilHeader().getPageType();
        if (pageType != FilPageType.SDI && pageType != FilPageType.INDEX)
            throw new IllegalArgumentException("Cannot traverse data records on non-index page");
        int leftMostLeaf = reachLeftMostSdiLeafLevel();
        PageDataLayout pageData = pages[leftMostLeaf].<IndexLayout>getContent().getPageData();

        pageData.setRecordDirectories(SDI_RECORD_DIRECTORIES);
        return pageData.dataRecords.values().stream()
                .peek(
                        rec -> {
                            if (rec.externalData) {
                                throw new UnsupportedOperationException(
                                        "SDI records with external data");
                            }
                        });
    }

    private int reachLeftMostSdiLeafLevel() {

        int pageNo = getSdiPageNo();
        IndexLayout sdiIndex = pages[pageNo].<IndexLayout>getContent();
        IndexHeaderLayout indexHeader = sdiIndex.getIndexHeader();
        //        int nRecords = indexHeader.getnUserRecords();
        // if (indexHeader.getLevel() == Univ.UINT64_MAX) throw new IllegalStateException("Cannot
        // reach to level zero");
        if (indexHeader.getnUserRecords() == 0)
            throw new IllegalStateException("Number of records is zero, nothing to process");
        while (indexHeader.getLevel() != 0) {
            sdiIndex.getPageData().setRecordDirectories(SDI_INTERNAL_RECORD_DIRECTORIES);
            SortedMap<Short, DataRecord> records = sdiIndex.getPageData().dataRecords;
            ByteBuffer data = ByteBuffer.wrap(records.get(records.firstKey()).rawData);
            long sdiId = data.getLong();
            int type = data.getInt();
            pageNo = data.getInt();
            indexHeader = pages[pageNo].<IndexLayout>getContent().getIndexHeader();
        }
        return pageNo;
    }
}
