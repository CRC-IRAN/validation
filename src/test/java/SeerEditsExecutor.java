import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;
import com.imsweb.validation.ValidationEngine;
import com.imsweb.validation.ValidatorContextFunctions;
import com.imsweb.validation.ValidatorServices;
import com.imsweb.validation.XmlValidatorFactory;
import com.imsweb.validation.entities.Rule;
import com.imsweb.validation.entities.RuleFailure;
import com.imsweb.validation.entities.SimpleNaaccrLinesValidatable;
import com.imsweb.validation.entities.Validator;

public class SeerEditsExecutor {

    public static void main(String[] args) throws Exception {

        File editsFile = new File(args[0]);
        File dataDir = new File(args[1]);
        File outputDir = new File(args[2]);

        // initialize validation engine
        if (!editsFile.exists())
            throw new Exception("Unable to find edits file!");
        ValidatorServices.initialize(new ValidatorServices());
        ValidatorContextFunctions.initialize(new ValidatorContextFunctions());
        Validator validator = XmlValidatorFactory.loadValidatorFromXml(editsFile);
        System.out.println("Loaded SEER edits version " + validator.getVersion() + " with " + validator.getRules().size() + " edits...");
        ValidationEngine.initialize(validator);
        System.out.println("Initialized validation engine with those edits...");

        // ignore the inter-record edits since this scripts doesn't support them anyway
        Set<String> toIgnore = new HashSet<>();
        for (Rule rule : validator.getRules())
            if ("lines".equals(rule.getJavaPath()))
                toIgnore.add(rule.getId());
        ValidationEngine.massUpdateIgnoreFlags(toIgnore, null);
        System.out.println("Disabled " + toIgnore.size() + " inter-record edits...");

        // get a sorted list of the edits, the flags will be created in that order in the new files
        NavigableMap<Rule, Boolean> sortedRules = new TreeMap<>(new RuleComparator());
        for (Rule r : validator.getRules())
            sortedRules.put(r, !toIgnore.contains(r.getId()));

        // create a CSV file with edits information
        File csvEditsFile = new File(outputDir, "edit-flags-description.csv");
        CSVWriter csvWriter = new CSVWriter(new FileWriter(csvEditsFile));
        csvWriter.writeNext("Column", "Edit ID", "Category", "Message", "Involved Property Names");
        int lineLength = ((FixedColumnsLayout)LayoutFactory.getLayout("naaccr-15-incidence")).getLayoutLineLength();
        int idx = 1;
        for (Map.Entry<Rule, Boolean> entry : sortedRules.entrySet()) {
            Rule r = entry.getKey();
            StringBuilder buf = new StringBuilder();
            String col = String.valueOf(lineLength + idx++);
            for (String prop : r.getRawProperties())
                buf.append(prop.replace("line.", "")).append(",");
            if (buf.length() > 0)
                buf.setLength(buf.length() - 1);
            csvWriter.writeNext(col, r.getId(), r.getCategory(), r.getMessage(), buf.toString());
        }
        csvWriter.flush();
        csvWriter.close();
        System.out.println("Created " + csvEditsFile.getName());

        // also copy the NAACCR 15 Incidence layout
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("layout/fixed/naaccr/naaccr-15-layout.xml");
        if (is == null)
            throw new RuntimeException("Could not find naaccr-15-layout.xml");
        File layoutFile = new File(outputDir, "naaccr-15-layout.xml");
        IOUtils.copy(is, new FileOutputStream(layoutFile));
        System.out.println("Created " + layoutFile.getName());

        // make sure the output folder exists
        if (!outputDir.exists())
            throw new Exception("Unable to find output folder!");

        // let's analyze the data files and process the records
        if (!dataDir.exists())
            throw new Exception("Unable to find data folder!");
        for (File dataFile : dataDir.listFiles()) {

            List<LayoutInfo> info = LayoutFactory.discoverFormat(dataFile);
            if (info.isEmpty()) {
                System.err.println("Unable to recognize the type of the file " + dataFile.getName() + "; skipping it!");
                continue;
            }
            System.out.println("Editing " + dataFile.getName() + "...");
            NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(info.get(0).getLayoutId());

            // create the output file
            File outputFile = new File(outputDir, dataFile.getName().replace(".txd.gz", "-flags.txd.gz"));
            GZIPOutputStream fos = new GZIPOutputStream(new FileOutputStream(outputFile));

            // now we are finally ready to validate the records
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataFile))));
            String line = reader.readLine();
            int count = 0;
            while (line != null) {
                count++;

                // re-write the data line
                fos.write(line.getBytes());

                // validate the line as a NAACCR record
                Collection<RuleFailure> failures = ValidationEngine.validate(new SimpleNaaccrLinesValidatable(layout.createRecordFromLine(line)));

                // write the flags
                StringBuilder buf = new StringBuilder();
                for (Map.Entry<Rule, Boolean> entry : sortedRules.entrySet())
                    buf.append(entry.getValue() ? (failures.contains(entry.getKey().getId()) ? "1" : "0") : " ");
                buf.append("\n");
                fos.write(buf.toString().getBytes());

                line = reader.readLine();
            }

            fos.close();
            reader.close();

            System.out.println("  > done editing " + count + " records from " + dataFile.getName());

        }
    }

    // this is the class that determines the order of the flags, so it's important... We use the same logic in SEER*Edits...
    public static class RuleComparator implements Comparator<Rule> {

        public static final Pattern RULE_ID_PATTERN = Pattern.compile("^(.+?)(\\d+)(.+)?");

        @Override
        public int compare(Rule r1, Rule r2) {
            String id1 = r1.getId();
            String id2 = r2.getId();

            Matcher m1 = RULE_ID_PATTERN.matcher(id1);
            Matcher m2 = RULE_ID_PATTERN.matcher(id2);

            if (m1.matches() && m2.matches()) {
                String prefix1 = m1.group(1);
                String prefix2 = m2.group(1);
                String integerPart1 = m1.group(2);
                String integerPart2 = m2.group(2);
                String suffix1 = m1.group(3);
                String suffix2 = m2.group(3);

                int result = prefix1.compareToIgnoreCase(prefix2);
                if (result == 0) {
                    Integer i1 = Integer.valueOf(integerPart1);
                    Integer i2 = Integer.valueOf(integerPart2);
                    result = i1.compareTo(i2);
                    if (result == 0) {
                        if (suffix1 == null)
                            return -1;
                        else if (suffix2 == null)
                            return 1;
                        else
                            return suffix1.compareToIgnoreCase(suffix2);
                    }
                    else
                        return result;
                }
                else
                    return result;
            }
            else
                return id1.toUpperCase().compareTo(id2.toUpperCase());
        }
    }
}