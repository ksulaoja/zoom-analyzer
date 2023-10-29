package ee.taltech.zoomalyzer.services;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import lombok.AllArgsConstructor;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

import static ee.taltech.zoomalyzer.util.Utils.generateRandomToken;

@Service
@AllArgsConstructor
public class RecordingService {
    private final RecordingDal recordingDal;
    private final Logger logger = Logger.getLogger(RecordingService.class.getSimpleName());


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
        if (isMeetingIdValid(recording.getMeetingId())) {
            recording.setToken(generateRandomToken(16));
            Recording newRecording = recordingDal.save(recording);
            logger.info(String.format("Generated token '%s' for recording '%s'.", recording.getToken(), recording.getId()));
            return newRecording;
        }
        throw new RuntimeException("Not valid meeting id");
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
