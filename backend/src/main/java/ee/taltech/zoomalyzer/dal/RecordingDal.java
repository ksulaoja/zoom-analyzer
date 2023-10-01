package ee.taltech.zoomalyzer.dal;

import ee.taltech.zoomalyzer.entities.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingDal extends JpaRepository<Recording, Long> {
}
