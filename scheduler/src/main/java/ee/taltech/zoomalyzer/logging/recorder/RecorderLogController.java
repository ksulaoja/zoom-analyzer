package ee.taltech.zoomalyzer.logging.recorder;

import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.general.LogLevelService;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLog;
import ee.taltech.zoomalyzer.logging.recorder.entities.RecorderLogDto;
import ee.taltech.zoomalyzer.services.RecordingService;
import ee.taltech.zoomalyzer.util.TimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/log/recorder")
@AllArgsConstructor
public class RecorderLogController {
    private static final Logger logger = Logger.getLogger(RecorderLogController.class.getSimpleName());

    private final RecordingService recordingService;
    private final LogLevelService logLevelService;
    private final RecorderLogService recorderLogService;


    @PostMapping
    public RecorderLogDto sendLog(@RequestBody RecorderLogDto logDto) {
        RecorderLog log = toRecorderLog(logDto);
        return toDto(recorderLogService.save(log));
    }

    @GetMapping("/local/{recordingId}")
    public String getLocalLogs(@PathVariable("recordingId") Long recordingId) {
        Recording recording = recordingService.findById(recordingId);
        return recorderLogService.getLocalLog(recording);
    }

    @GetMapping("/{recordingId}")
    @CrossOrigin(origins = "*")
    public List<RecorderLogDto> getLogs(@PathVariable("recordingId") Long recordingId, @RequestParam(name = "level", required = false) String level) {
        Recording recording = recordingService.findById(recordingId);
        if (level != null && !level.isBlank()) {
            return recorderLogService.getLogsByLevel(recording, level).stream().map(this::toDto).toList();
        }
        return recorderLogService.getLogs(recording).stream().map(this::toDto).toList();
    }

    private RecorderLog toRecorderLog(RecorderLogDto dto) {
        RecorderLog log = new RecorderLog();
        log.setMessage(dto.getMessage());
        log.setRecording(recordingService.findById(dto.getRecordingId()));
        log.setLogLevel(logLevelService.findByName(dto.getLogLevel()));
        log.setTime(Instant.now());
        return log;
    }

    private RecorderLogDto toDto(RecorderLog log) {
        if (log == null) {
            return null;
        }
        RecorderLogDto dto = new RecorderLogDto();
        dto.setRecordingId(log.getRecording().getId());
        dto.setTime(TimeUtils.toIso8601(log.getTime()));
        dto.setMessage(log.getMessage());
        dto.setLogLevel(log.getLogLevel().getName());
        dto.setId(log.getId());
        return dto;
    }
}
