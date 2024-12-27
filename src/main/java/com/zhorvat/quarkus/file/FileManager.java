package com.zhorvat.quarkus.file;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

@ApplicationScoped
public class FileManager {

    private final String fileName;

    private final YamlHandler yamlHandler;

    @Inject
    public FileManager(
            @ConfigProperty(name = "prometheusFile.name") String fileName,
            YamlHandler yamlHandler
    ) {
        this.fileName = fileName;
        this.yamlHandler = yamlHandler;
    }

    public void writeToPrometheusYaml(String jobDetails) {
        try (FileWriter writer = new FileWriter(fileName)) {
            yamlHandler.writeYamlFile(writer, fileName, jobDetails);
        } catch (IOException e) {
            throw new RuntimeException("There was an error, while writing to file", e);
        }
    }

    public String readFromPrometheusYaml() {
        try (RandomAccessFile accessFile = new RandomAccessFile(new File(fileName), "rw")) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = accessFile.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("There was an error, while reading from file", e);
        }
    }
}
