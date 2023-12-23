package ee.taltech.zoomalyzer.services;

import ee.taltech.zoomalyzer.conf.RecorderConfig;
import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.util.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private final RecorderConfig recorderConfig;


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
            String filePath = recorderConfig.getRecorderPath() + "/" + getUniqueName(recording) + "-1.csv";

            File file = new File(filePath);

            if (file.exists()) {
                logger.info("Analysis already exists");
                return;
            }

            String virtualEnvActivateCommand = recorderConfig.getRootPath() + "/analyzer/venv/Scripts/activate";
            String script = virtualEnvActivateCommand + " && python " + "./analyzer/analyzer.py " + recorderConfig.getToken() + " " + recorderConfig.getRecorderPath() + "/" + getUniqueName(recording);
            String[] commandToExecute = new String[]{"cmd.exe", "/c", script};
            Process p = Runtime.getRuntime().exec(commandToExecute);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of analyzer:\n");
            String s;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the analyzer (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            // Close the input streams
            stdInput.close();
            stdError.close();

            // Wait for the process to exit
            int exitCode = p.waitFor();
            System.out.println("Analyzer Process exited with code " + exitCode);
        }
        catch (IOException | InterruptedException e) {
            System.out.println("exception happened - here's what I know: ");
            logger.warning("Analyzer failed");
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
