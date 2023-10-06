package ee.taltech.zoomalyzer.logging.general;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogLevelService {
    private final LogLevelDal logLevelDal;

    public LogLevel findByName(String name) {
        // TODO entity not found
        return logLevelDal.findByName(name).orElseThrow();
    }
}
