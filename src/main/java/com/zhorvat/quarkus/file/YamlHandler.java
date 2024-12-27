package com.zhorvat.quarkus.file;

import jakarta.enterprise.context.ApplicationScoped;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.channels.FileChannel;

@ApplicationScoped
public class YamlHandler {

    public void writeYamlFile(FileWriter writer, String fileName, String content) throws IOException {
        File inputFile = new File(fileName);
        writeToYamlFile(writer, content);
        removeDanglingCharacter(inputFile);
        copyFileContent(inputFile);
    }

    private static void writeToYamlFile(FileWriter writer, String content) {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);

        Yaml yaml = new Yaml(options);
        yaml.dump(content, writer);
    }

    private static void removeDanglingCharacter(File inputFile) throws IOException {
        File tempFile = new File("src/main/resources/prometheusTemp.yml");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));

        String lineToRemove = "|-";
        String currentLine;

        while ((currentLine = reader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if (trimmedLine.equals(lineToRemove)) continue;
            bufferedWriter.write(currentLine + System.getProperty("line.separator"));
        }
        bufferedWriter.close();
        reader.close();
    }

    private static void copyFileContent(File inputFile) throws IOException {
        File tempFile = new File("src/main/resources/prometheusTemp.yml");
        try (FileChannel src = new FileInputStream(tempFile).getChannel();
             FileChannel dest = new FileOutputStream(inputFile).getChannel()) {
            dest.transferFrom(src, 0, src.size());
            src.close();
            dest.close();
            tempFile.delete();
        }
    }
}
