package ee.taltech.zoomalyzer.logging.recorder;

import ee.taltech.zoomalyzer.conf.RecorderConfig;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

import static ee.taltech.zoomalyzer.util.Utils.getUniqueName;

@Service
@RequiredArgsConstructor
public class RecorderLogService {
    private final RecorderLogDal recorderLogDal;
    private final RecorderConfig recorderConfig;
    private static final Logger logger = Logger.getLogger(RecorderLogService.class.getSimpleName());

    public RecorderLog save(RecorderLog recorderLog) {
        recorderLog.setTime(Instant.now());
        logger.info(recorderLog.toString());
        return recorderLogDal.save(recorderLog);
    }

    public List<RecorderLog> getLogs(Recording recording) {
        return recorderLogDal.findAllByRecording(recording);
    }

    public List<RecorderLog> getLogsByLevel(Recording recording, String logLevelName) {
        return recorderLogDal.findAllByRecordingAndLogLevel_NameOrderByIdAsc(recording, logLevelName);
    }

    public String getLocalLog(Recording recording) {
        Path path = Paths.get(recorderConfig.getPath(), "screenshots", getUniqueName(recording) + ".log");
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                return new String(Files.readAllBytes(path));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("File does not exist:" + path);
    }
}
