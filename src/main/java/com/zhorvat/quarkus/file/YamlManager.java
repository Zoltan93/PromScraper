package com.zhorvat.quarkus.file;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class YamlManager {

    private final String fileName;

    @Inject
    public YamlManager(
            @ConfigProperty(name = "prometheusFile.name") String fileName
    ) {
        this.fileName = fileName;
    }

    public void writeToPrometheusYaml(String content) {
        try {
            writeToYaml(content);
            removeDanglingCharacter();
        } catch (IOException e) {
            throw new RuntimeException("There was an issue, while writing to the file", e);
        }
    }

    public String read() {
        try {
            return FileUtils.readFileToString(new File(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("There was an issue, while reading from the file", e);
        }
    }

    void writeToYaml(String content) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndicatorIndent(2);
        options.setIndentWithIndicator(true);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.LITERAL);
        try (FileWriter writer = new FileWriter(fileName)) {
            Yaml yaml = new Yaml(options);
            yaml.dump(content, writer);
        }
    }

    void removeDanglingCharacter() throws IOException {
        File tempFile = new File("src/main/resources/prometheusTemp.yml");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile));

        String lineToRemove = "|-";
        String currentLine;
        while ((currentLine = reader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if (trimmedLine.equals(lineToRemove)) {
                continue;
            }
            bufferedWriter.write(currentLine + System.lineSeparator());
        }
        bufferedWriter.close();
        reader.close();
        copyFileContent();
    }

    void copyFileContent() throws IOException {
        File tempFile = new File("src/main/resources/prometheusTemp.yml");
        try (FileChannel src = new FileInputStream(tempFile).getChannel();
             FileChannel dest = new FileOutputStream(fileName).getChannel()) {
            dest.transferFrom(src, 0, src.size());
            src.close();
            dest.close();
            tempFile.delete();
        }
    }
}
