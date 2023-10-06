package ee.taltech.zoomalyzer.logging.general;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogLevelDal extends JpaRepository<LogLevel, Long> {
    Optional<LogLevel> findByName(String name);
}
