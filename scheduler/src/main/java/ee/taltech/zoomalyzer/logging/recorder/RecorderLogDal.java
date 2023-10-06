package ee.taltech.zoomalyzer.logging.recorder;

import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecorderLogDal extends JpaRepository<RecorderLog, Long> {
}
