package com.zhorvat.quarkus.file;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;

@ApplicationScoped
public class FileManager {

    private final String fileName;

    private final YamlHandler yamlHandler;

    @Inject
    public FileManager(
            @ConfigProperty(name = "podFile.name") String fileName,
            YamlHandler yamlHandler
    ) {
        this.fileName = fileName;
        this.yamlHandler = yamlHandler;
    }

    public boolean isPodPresent(String podName) {
        return readFromFile().contains(podName);
    }

    public void writeToFile(String fileName, String jobDetails) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            yamlHandler.handle(writer, fileName, jobDetails);
        } catch (IOException e) {
            throw new RuntimeException("There was an error, while writing to file", e);
        }
    }

    public String readFromFile() {
//        ClassLoader classLoader = this.getClass().getClassLoader();
//        try (InputStream stream = classLoader.getResourceAsStream(fileName);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
//            StringBuilder stringBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                stringBuilder.append(line).append("\n");
//            }
//            return stringBuilder.toString();
        try(RandomAccessFile accessFile = new RandomAccessFile(new File(fileName), "rw")) {
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

    private void writeToFile(String podDetails) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(podDetails);
        } catch (IOException e) {
            throw new RuntimeException("There was an error, while writing to file", e);
        }
    }
}
