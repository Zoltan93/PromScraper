package com.zhorvat.quarkus.prometheus;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;

@ApplicationScoped
public class FileManager {

    private final String fileName;

    public FileManager(
            @ConfigProperty(name = "podFile.name") String fileName
    ) {
        this.fileName = fileName;
    }

    public boolean isPodPresent(String podName) {
        return readFromFile(fileName).contains(podName);
    }

    public void writeToFile(String fileName, String jobDetails) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(jobDetails);
        } catch (IOException e) {
            throw new RuntimeException("There was an error, while writing to file", e);
        }
    }

    private String readFromFile(String fileName) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
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
