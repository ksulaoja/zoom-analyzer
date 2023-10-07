package ee.taltech.zoomalyzer.scheduler;

import ee.taltech.zoomalyzer.conf.RecorderConfig;
import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.recorder.RecorderLogController;
import ee.taltech.zoomalyzer.logging.recorder.RecorderLogService;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLogDto;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static ee.taltech.zoomalyzer.util.Utils.getUniqueName;

@Service
@AllArgsConstructor
public class RecordingScheduler {
    private final RecordingDal recordingDal;
    private final RecorderLogService recorderLogService;
    private final RecorderLogController recorderLogController;
    private final ScheduledExecutorService scheduleExecutorService = Executors.newScheduledThreadPool(5);
    private final ScheduledExecutorService statusExecutorService = Executors.newScheduledThreadPool(5);
    private final Logger logger = Logger.getLogger(RecordingScheduler.class.getSimpleName());
    private final RecorderConfig recorderConfig;

    @Scheduled(fixedRate = 10000) // change to 10min
    public void checkSchedule() {
        Instant now = Instant.now();
        List<Recording> upcoming = recordingDal.findByStartTimeGreaterThanAndScheduledIsFalse(now);

        for (Recording recording : upcoming) {
            // TODO factor in startup time (users expect the recording to start at that certain time)
            Duration delay = Duration.between(now, recording.getStartTime());

            if (!delay.isNegative()) {
                long delayMillis = delay.toMillis();
                scheduleExecutorService.schedule(() -> startRecorder(recording), delayMillis, TimeUnit.MILLISECONDS);
                recording.setScheduled(true);
                recordingDal.save(recording);
                logger.info(String.format("Added to executorService: %s", recording));
            }
        }
    }

    public void checkStatus(Recording recording) {
        logger.info("Checking status of recording");
        List<RecorderLog> logs = recorderLogService.getLogsByLevel(recording, "STATUS");
        List<String> messages = logs.stream().map(RecorderLog::getMessage).toList();
        Optional<RecorderLog> statusRecording = logs.stream()
                .filter(l -> l.getLogLevel().getName().equals("RECORDING"))
                .findFirst();

        String actionMsg = null;
        if (logs.isEmpty()) {
            sendStatusLog(recording, "STOPPED");
            actionMsg = "Recording logs are empty";
        }
        else if (messages.contains("FAILED") || messages.contains("ENDED")) {
            actionMsg = "Recording has failed/ended";
        }
        else if (statusRecording.isEmpty() &&
                recording.getStartTime().plusSeconds(recorderConfig.getMaxJoiningTime()).isBefore(Instant.now())) {
            actionMsg = "Recording max joining time reached";
        }
        else if (statusRecording.isPresent() &&
                statusRecording.get().getTime().plusSeconds(recording.getDuration() * 60L + 60)
                        .isBefore(Instant.now())) {
            actionMsg = "Recording duration reached";
        }

        if (actionMsg == null) {
            statusExecutorService.schedule(() -> checkStatus(recording), recorderConfig.getStatusCheckPeriod(), TimeUnit.SECONDS);
        } else {
            logger.info(actionMsg);
            stopRecorder(recording);
        }
    }

    private void startRecorder(Recording recording) {
        stopRecorder(recording);
        logger.info(String.format("Start docker container to record: %s", recording));
        // TODO validate, sanitize meeting id & password in RecordingService
        String startCommand = String.format("docker run -d -e MEETING_ID=%s -e MEETING_PASSWORD=%s -e MEETING_DURATION=%s " +
                        "-e RECORDING_ID=%s --name %s -v %s:/home/zoomrec/recordings " +
                        "--security-opt seccomp:unconfined %s",
                recording.getMeetingId(), recording.getMeetingPw(), recording.getDuration(),
                recording.getId(), getUniqueName(recording), recorderConfig.getPath(), recorderConfig.getImage()
                );

        try {
            runProcess(startCommand);
        } catch (InterruptedException | IOException e) {
            logger.severe(e.getMessage());
            sendStatusLog(recording, "SCHEDULER FAILED");
        }
        statusExecutorService.schedule(() -> checkStatus(recording), recorderConfig.getStatusCheckPeriod(), TimeUnit.SECONDS);
    }

    public void stopRecorder(Recording recording) {
        String container = getUniqueName(recording);
        logger.info("Stopping container " + container);
        String stopCommand = String.format("docker stop %s && docker rm %s",
                container, container);
        try {
            runProcess(stopCommand);
        } catch (InterruptedException | IOException e) {
            logger.warning(e.getMessage());
        }
    }



    private void runProcess(String command) throws IOException, InterruptedException {
        ProcessBuilder startProcessBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        Process process = startProcessBuilder.start();
        InputStream stdout = process.getInputStream();
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
        String line;
        /*StringBuilder stdoutBuilder = new StringBuilder();

        // Read all lines of standard output
        while ((line = stdoutReader.readLine()) != null) {
            stdoutBuilder.append(line).append('\n');
        }*/

        // Capture standard error
        InputStream stderr = process.getErrorStream();
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
        StringBuilder stderrBuilder = new StringBuilder();

        // Read all lines of standard error
        while ((line = stderrReader.readLine()) != null) {
            stderrBuilder.append(line).append('\n');
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();

        // Print the captured output
        if (exitCode == 1) {
            logger.warning("STDERR:\n" + stderrBuilder);
        }
    }

    private void sendStatusLog(Recording recording, String message) {
        RecorderLogDto log = new RecorderLogDto();
        log.setLogLevel("STATUS");
        log.setRecordingId(recording.getId());
        log.setMessage(message);
        recorderLogController.sendLog(log);
    }
}
