package parser;

import context.PageContext;
import formatter.CommandLineIndentFormatter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import parserGen.Page;

public class InnodbParser {
    public static void main(String[] args) throws IOException {

        String filePath = (String) Array.get(args, 0);
        //                args.length == 0
        //                        ? Objects.requireNonNull(
        //                                        InnodbParser.class
        //                                                .getClassLoader()
        //                                                .getResource("samples/departments.ibd"))
        //                                .getPath()
        //                        : args[0];
        FileChannel fileChannel = FileChannel.open(Path.of(filePath), StandardOpenOption.READ);
        final long nPages = fileChannel.size() / 1024 / 16;
        PageLayout[] pages = new PageLayout[Math.toIntExact(nPages)];
        for (int i = 0; i < nPages; i++) {
            try {
                pages[i] = new Page(fileChannel);
            } catch (UnsupportedOperationException e) {
                // System.err.println(e);
            }
        }
        PageContext context = new PageContext(new CommandLineIndentFormatter(), pages);

        context.item("==========SDI INFO==========");
        context.traverseSdiDataRecords()
                .forEach(
                        rec -> {
                            Map<String, Object> objects =
                                    PageDataLayout.parseRawData(
                                            rec,
                                            IndexLayout.SDI_RECORD_DIRECTORIES,
                                            map -> {
                                                Inflater inflater = new Inflater();
                                                int sdiCompLen = (Integer) map.get("COMP");
                                                int sdiUncompLen = (Integer) map.get("UNCOMP");
                                                byte[] comp = (byte[]) map.get("LOAD");
                                                if (comp.length != sdiCompLen) {
                                                    throw new IllegalStateException(
                                                            "Row record inconsistency found:"
                                                                + " compressed length is not equal"
                                                                + " with read length");
                                                }
                                                inflater.setInput(comp, 0, sdiCompLen);
                                                byte[] uncomp = new byte[sdiUncompLen + 1];
                                                try {
                                                    inflater.inflate(uncomp);
                                                } catch (DataFormatException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                inflater.end();
                                                map.put("LOAD", new String(uncomp));
                                            });

                            for (int i = 0; i < objects.size(); i++) {
                                String colName = IndexLayout.SDI_RECORD_DIRECTORIES.get(i).name;
                                context.item(String.format("<%s>", colName));
                                String content =
                                        IndexLayout.SDI_RECORD_DIRECTORIES
                                                .get(i)
                                                .targetClass
                                                .cast(objects.get(colName))
                                                .toString();
                                context.subItem(f -> f.item(content));
                            }
                        });
        context.item("==========SDI INFO END==========");
        context.item("");
        for (final PageLayout page : pages) {
            if (page != null) page.format(context);
        }
        System.out.println(context);
    }
}
