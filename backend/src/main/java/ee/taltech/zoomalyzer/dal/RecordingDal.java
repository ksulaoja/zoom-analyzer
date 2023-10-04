package ee.taltech.zoomalyzer.dal;

import ee.taltech.zoomalyzer.entities.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RecordingDal extends JpaRepository<Recording, Long> {

    List<Recording> findByStartTimeGreaterThanAndFilePathIsNullAndScheduledIsFalse(Instant startTime);
}
