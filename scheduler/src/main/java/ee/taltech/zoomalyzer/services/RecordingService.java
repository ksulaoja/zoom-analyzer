package ee.taltech.zoomalyzer.services;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.util.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static ee.taltech.zoomalyzer.util.Utils.generateRandomToken;
import static ee.taltech.zoomalyzer.util.Utils.getUniqueName;

@Service
@AllArgsConstructor
public class RecordingService {
    private final RecordingDal recordingDal;
    private static final Logger logger = Logger.getLogger(RecordingService.class.getSimpleName());
    private final EmailService emailService;


    public List<Recording> findAll() {
        return recordingDal.findAll();
    }

    public Recording findById(Long id) {
        // TODO entity not found
        return recordingDal.findById(id).orElseThrow();
    }

    public Recording save(Recording recording) {
        if (recording.getId() != null && recordingDal.findById(recording.getId()).isPresent()) {
            throw new RuntimeException("Recording exists");
        }
        // TODO validate/sanitize meetingId, password, email
        // TODO if using meeting id (not url), check for password
        if (!isMeetingIdValid(recording.getMeetingId())) {
            throw new RuntimeException("Not valid meeting id");
        }
        recording.setToken(generateRandomToken(16));
        Recording newRecording = recordingDal.save(recording);
        logger.info(String.format("Generated token '%s' for recording '%s'.", recording.getToken(), recording.getId()));
        try {
            emailService.sendSavedRecordingEmail(recording);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send recording email to " + recording.getUserEmail());
        }
        // TODO add recording activation link to email
        return newRecording;
    }

    public void startAnalysis(Recording recording) {
        try {
            String filename = getUniqueName(recording) + ".mkv";
            filename = "video1624200094.wav";
            String command = "python C:\\Users\\kristjan\\zoom-analyzer\\analyzer\\analyzer.py " + filename;

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            processBuilder.redirectErrorStream(true); // Redirect error stream to input stream

            Process process = processBuilder.start();

            // Wait for the process to finish (optional)
            int exitCode = process.waitFor();

            // Check the exit code to determine if the process was successful
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.err.println("Error executing Python script. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isMeetingIdValid(String meetingId) {
        if (meetingId.matches("^\\d*$") && meetingId.length() >= 10 && meetingId.length() <= 11) {
            return true;
        } else {
            UrlValidator validator = new UrlValidator();
            return validator.isValid(meetingId);
        }
    }
}
