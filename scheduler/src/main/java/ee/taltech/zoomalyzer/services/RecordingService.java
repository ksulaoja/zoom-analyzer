package ee.taltech.zoomalyzer.services;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RecordingService {
    private final RecordingDal recordingDal;

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
        return recordingDal.save(recording);
    }
}
