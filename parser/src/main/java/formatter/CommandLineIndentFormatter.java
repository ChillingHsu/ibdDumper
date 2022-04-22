package formatter;

public class CommandLineIndentFormatter implements Formatter {
    private final StringBuilder sb;
    private int indent;
    private String indentDelimiter = "\t";
    private final boolean compact = true;
    private String lastItemCache;
    private int compactCounter = 0;
    private final String COMPACT_TEMPLATE = "Ditto the above for %d time(s)";
    boolean preparing = false;

    public CommandLineIndentFormatter() {
        this.sb = new StringBuilder();
        this.indent = 0;
    }

    public CommandLineIndentFormatter(String indentDelimiter) {
        this();
        this.indentDelimiter = indentDelimiter;
    }

    private void flushCompactContentIfPresent() {
        if (compact && lastItemCache != null) {
            if (compactCounter > 0) {
                append(COMPACT_TEMPLATE.formatted(compactCounter));
                end();
            }
            compactCounter = 0;
            lastItemCache = null;
        }
    }

    @Override
    public String toString() {
        flushCompactContentIfPresent();
        return this.sb.toString();
    }

    @Override
    public void child() {
        flushCompactContentIfPresent();
        ++this.indent;
    }

    @Override
    public void parent() {
        flushCompactContentIfPresent();
        --this.indent;
        if (this.indent < 0) throw new IllegalStateException("indent downflows");
    }

    @Override
    public void item(String content) {
        if (content == null) return;
        if (compact && content.equals(lastItemCache)) {
            ++compactCounter;
            return;
        }
        if (compact) {
            flushCompactContentIfPresent();
            lastItemCache = content;
        }
        insertIndent();
        append(content);
        end();
    }

    @Override
    public void append(String content) {
        insertIndent();
        sb.append(content);
    }

    @Override
    public void end() {
        if (!preparing) return;
        sb.append("\n");
        preparing = false;
    }

    private void insertIndent() {
        if (preparing) return;
        sb.append(indentDelimiter.repeat(this.indent));
        preparing = true;
    }
}
