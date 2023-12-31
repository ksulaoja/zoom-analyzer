package ee.taltech.zoomalyzer.conf;

import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.general.LogLevel;
import ee.taltech.zoomalyzer.logging.general.LogLevelDal;
import ee.taltech.zoomalyzer.services.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
public class Configuration {
    private final Logger logger = Logger.getLogger(Configuration.class.getSimpleName());

    @Bean
    CommandLineRunner commandLineRunner(RecordingService recordingService, LogLevelDal logLevelDal, RecorderConfig recorderConfig) {
        return env -> {

            if (!isDockerInstalled()) {
                throw new RuntimeException("Docker not installed");
            }
            if (!validateRecordingDirectory(recorderConfig.getRecorderPath(), true)) {
                throw new RuntimeException("Can't use recorder path " + recorderConfig.getRecorderPath());
            }
            if (!imageExists(recorderConfig.getImage())) {
                throw new RuntimeException("Docker image " + recorderConfig.getImage() + " is not available");
            }

            /*Recording recording = new Recording();
            recording.setDuration(5);
            recording.setStartTime(Instant.now().plusSeconds(100000000L));
            recording.setMeetingId("https://us04web.zoom.us/j/79046434327?pwd=MgaoVWJVcGoyJyUJ6UVSgaTfA11aX1.1");
            recording.setMeetingPw("911647");
            recording.setUserEmail("paulbryantan@gmail.com");
            recordingService.save(recording);*/

            LogLevel info = new LogLevel("INFO");
            LogLevel debug = new LogLevel("DEBUG");
            LogLevel error = new LogLevel("ERROR");
            LogLevel status = new LogLevel("STATUS");
            logLevelDal.saveAll(List.of(info, debug, error, status));
        };
    }

    private boolean isDockerInstalled() {
        ProcessBuilder processBuilder = new ProcessBuilder("docker", "--version");
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.severe("Could not find docker on host");
            return false;
        }
    }

    private boolean validateRecordingDirectory(String recorderPath, boolean checkLogDirectory) {
        Path path = Paths.get(recorderPath);

        // Check if the directory exists
        if (!Files.exists(path)) {
            logger.info("Created directory " + recorderPath);
            try {
                // Create the directory if it doesn't exist
                Files.createDirectories(path);
            } catch (IOException e) {
                logger.severe("Failed to create recordings directory " + recorderPath);
                return false;
            }
        }
        if (checkLogDirectory) {
            return validateRecordingDirectory(Paths.get(recorderPath, "/screenshots").toString(), false);
        }
        return true;
    }

    private boolean imageExists(String imageName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker", "images");

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(imageName)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            logger.severe("Checking if docker exists failed");
            return false;
        }
    }
}
