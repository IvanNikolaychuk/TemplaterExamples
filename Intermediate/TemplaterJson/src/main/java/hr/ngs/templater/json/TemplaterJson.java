package hr.ngs.templater.json;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.ObjectConverter;
import hr.ngs.templater.Configuration;
import hr.ngs.templater.DocumentFactoryBuilder;
import hr.ngs.templater.TemplateDocument;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/*

103680 records - 11.7 sec.
207360 records - 64 sec. (1 min)
311040 records - 172 sec. (3 min)

 */
public class TemplaterJson {
    private static final Integer APPROX_NUMBER_OF_TRANSACTIONS = 300_000;

    public static void main(String[] args) throws IOException {
        process(
                TemplaterJson.class.getResource("/template/template-masked.xlsx").getPath(),
                TemplaterJson.class.getResource("/template/data.json").getPath()

        );
    }

    public static int process(String templatePath, String dataPath) throws IOException {
        // Prepare the input template stream, check extension
        SupportedType st = SupportedType.getByFilename(templatePath);
        if (st == null) {
            System.err.println("Unsupported extension: " + templatePath);
            return 2;
        }

        InputStream templateStream = new FileInputStream(templatePath);

        // Prepare the input data stream (file or stdin)
        InputStream dataStream = dataPath == null ? System.in : new FileInputStream(dataPath);

        // Prepare the output stream (file or stdout)
        File tmp = File.createTempFile("test", ".xlsx");
        OutputStream outputStream = Files.newOutputStream(tmp.toPath());

        process(st.extension, templateStream, dataStream, outputStream);


        System.out.println(tmp.getPath());
//        Desktop.getDesktop().open(tmp);
        return 0;
    }

    private static Object readData(InputStream dataStream) throws IOException {
        DslJson<Object> dslJson = new DslJson<Object>(new DslJson.Settings<Object>());
        JsonReader<Object> reader = dslJson.newReader(dataStream, new byte[4096]);
        reader.getNextToken();
        LinkedHashMap result = (LinkedHashMap) ObjectConverter.deserializeObject(reader);

        java.util.List transactions = (java.util.List) result.get("transactions");

        java.util.List newTransactions = new ArrayList();

        while (newTransactions.size() < APPROX_NUMBER_OF_TRANSACTIONS) {
            newTransactions.addAll(transactions);
        }

        result.put("transactions", newTransactions);

        System.out.println("Running test on " + newTransactions.size() + " transactions");

        return result;
    }

    public static void process(
            String extension,
            InputStream templateStream,
            InputStream dataStream,
            OutputStream outputStream) throws IOException {

        Object data = readData(dataStream);

        TemplateDocument tpl = Configuration.builder()
                .include(IMAGE_DECODER)
                .build("/Users/ivan/IdeaProjects/TemplaterExamples/Advanced/TemplaterServer/templater.lic")
                .open(templateStream, extension, outputStream);

        Instant start = Instant.now();
        tpl.process(data);
        tpl.close();

        System.out.println(Duration.between(start, Instant.now()).toMillis());
    }

    private static DocumentFactoryBuilder.Formatter IMAGE_DECODER = new DocumentFactoryBuilder.Formatter() {
        @Override
        public Object format(Object value, String metadata) {
            if ("image".equals(metadata) && value instanceof String) {
                byte[] bytes = Base64.getDecoder().decode((String)value);
                try {
                    return ImageIO.read(new ByteArrayInputStream(bytes));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return value;
        }
    };

    private static void outputHelp(final PrintStream ps) {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format("Example usage:%n" +
                "\tjava -jar templater-json.jar template.ext [data.json] [output.ext]%n" +
                "\ttemplate.ext: path to the template file (eg. document.docx)%n" +
                "\tdata.json:    path to a file containing a JSON object or an array of JSON objects%n" +
                "\toutput.ext:   output path where the processed report is to be placed (eg. result.docx)%n%n" +
                "Alternatively, you can use omit the [data.json] and [output.ext] arguments to read from stdin and write to stdout%n" +
                "\tjava -jar templater-json.jar template.ext < [data.json] > [output.ext]%n%n" +
                "\tjava -jar templater-json.jar template.ext < [data.json] > [output.ext]%n%n" +
                "Images can be sent as base64 string in JSON and paired with :image metadata on the tag.%n%n" +
                "Supported extensions are:%n");

        for (SupportedType st : SupportedType.values()) {
            fmt.format("\t%-4s - %s%n", st.extension, st.description);
        }
        ps.print(sb);
    }
}
