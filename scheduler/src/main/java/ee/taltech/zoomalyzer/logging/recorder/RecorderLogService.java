package ee.taltech.zoomalyzer.logging.recorder;

import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class RecorderLogService {
    private final RecorderLogDal recorderLogDal;
    private static final Logger logger = Logger.getLogger(RecorderLogService.class.getSimpleName());

    public RecorderLog save(RecorderLog recorderLog) {
        switch (recorderLog.getLogLevel().getName()) {
            case "INFO":
            case "DEBUG":
                logger.info(recorderLog.toString());
                break;
            case "ERROR":
                logger.severe(recorderLog.toString());
        }
        return recorderLogDal.save(recorderLog);
    }
}
