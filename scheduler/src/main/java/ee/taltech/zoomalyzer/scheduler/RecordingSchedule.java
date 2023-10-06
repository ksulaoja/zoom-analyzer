package ee.taltech.zoomalyzer.scheduler;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class RecordingSchedule {
    private final RecordingDal recordingDal;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    private final Logger logger = Logger.getLogger(RecordingSchedule.class.getSimpleName());

    @Scheduled(fixedRate = 10000) // change to 10min
    public void checkSchedule() {
        Instant now = Instant.now();
        List<Recording> upcoming = recordingDal.findByStartTimeGreaterThanAndFilePathIsNullAndScheduledIsFalse(now);

        for (Recording recording : upcoming) {
            Duration delay = Duration.between(now, recording.getStartTime());

            if (!delay.isNegative()) {
                long delayMillis = delay.toMillis();
                executorService.schedule(() -> startRecording(recording), delayMillis, TimeUnit.MILLISECONDS);
                recording.setScheduled(true);
                recordingDal.save(recording);
                logger.info(String.format("Added to executorService: %s", recording));
            }
        }
    }

    private void startRecording(Recording recording) {
        logger.info(String.format("Start docker container to record: %s", recording));
        // TODO start recording in docker
        // TODO if status of startup not sent, stop container
        // TODO if status of recording not sent, stop container
        // TODO if status is failed, stop container
        // if no status updates, check log file /recordings/screenshots/recording-{recording_id}.log
    }
}
