package ee.taltech.zoomalyzer.logging.recorder;

import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecorderLogDal extends JpaRepository<RecorderLog, Long> {
    List<RecorderLog> findAllByRecording(Recording recording);
    List<RecorderLog> findAllByRecordingAndLogLevel_NameOrderByIdAsc(Recording recording, String logLevelName);

}
