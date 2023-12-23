package ee.taltech.zoomalyzer.controllers;

import ee.taltech.zoomalyzer.conf.RecorderConfig;
import ee.taltech.zoomalyzer.controllers.dtos.RecordingDto;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.services.RecordingService;
import ee.taltech.zoomalyzer.util.TimeUtils;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static ee.taltech.zoomalyzer.util.Utils.getUniqueName;
import static ee.taltech.zoomalyzer.util.Utils.validateToken;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/recordings")
public class RecordingController {

    private final RecordingService recordingService;
    private final RecorderConfig recorderConfig;

    @GetMapping
    @CrossOrigin(origins = "*")
    public List<RecordingDto> getRecordings() {
        // admin interface, to be removed
        return recordingService.findAll()
                .stream().map(this::toDto)
                .toList();
    }

    @PostMapping
    @CrossOrigin(origins = "*")
    public RecordingDto scheduleRecording(@RequestBody RecordingDto dto) {
        Recording recording = toRecording(dto);
        Recording savedRec = recordingService.save(recording);
        return toDto(savedRec);
    }

    @GetMapping("/{recordingId}")
    @CrossOrigin(origins = "*")
    public RecordingDto getRecording(@PathVariable("recordingId") Long recordingId, @RequestParam(name = "token", required = false) String token) {
        Recording recording = recordingService.findById(recordingId);
        validateToken(token, recording);
        return toDto(recordingService.findById(recordingId));
    }

    @GetMapping("/analyze/{recordingId}")
    @CrossOrigin(origins = "*")
    public String startAnalysis(@PathVariable("recordingId") Long recordingId, @RequestParam(name = "token", required = false) String token) {
        Recording recording = recordingService.findById(recordingId);
        validateToken(token, recording);
        recordingService.startAnalysis(recording);
        return "Done";
    }

    @GetMapping("/download/{recordingId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Resource> downloadRecording(@PathVariable("recordingId") Long recordingId,  @RequestParam(name = "token", required = false) String token) throws MalformedURLException {
        Recording recording = recordingService.findById(recordingId);
        validateToken(token, recording);
        String filename = getUniqueName(recording) + recorderConfig.getFileType();
        Path filePath = Paths.get(recorderConfig.getRecorderPath(),filename);

        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + generateFileName(recording, recorderConfig.getFileType()));
            MediaType mediaType = MediaType.parseMediaType("video/x-matroska");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/analyze/{recordingId}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Resource> downloadAnalysis(@PathVariable("recordingId") Long recordingId,  @RequestParam(name = "token", required = false) String token, @RequestParam(name = "ver", required = false) Integer version) throws IOException {
        Recording recording = recordingService.findById(recordingId);
        validateToken(token, recording);
        int fileVer = Objects.equals(version, 2) ? 2 : 1;
        String filename = getUniqueName(recording) + "-" + fileVer + ".csv";
        Path filePath = Paths.get(recorderConfig.getRecorderPath(),filename);

        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + generateFileName(recording, "-" + String.valueOf(fileVer) + ".csv"));
            MediaType mediaType = MediaType.parseMediaType("text/csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(mediaType)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    private static String generateFileName(Recording recording, String extension) {
        return String.format("meeting-%s-%s%s", recording.getId(), TimeUtils.toIso8601(recording.getStartTime(), "Europe/Tallinn"), extension);
    }

    private Recording toRecording(RecordingDto dto) {
        Recording recording = new Recording();
        recording.setMeetingId(dto.getMeetingId());
        recording.setMeetingPw(dto.getMeetingPw());
        recording.setDuration(dto.getRecordingLength());
        recording.setStartTime(TimeUtils.toInstant(dto.getStartTime()));
        recording.setUserEmail(dto.getUserEmail());
        return recording;
    }

    private RecordingDto toDto(Recording recording) {
        RecordingDto dto = new RecordingDto();
        dto.setId(recording.getId());
        dto.setMeetingId(recording.getMeetingId());
        dto.setMeetingPw(recording.getMeetingPw());
        dto.setRecordingLength(recording.getDuration());
        dto.setStartTime(TimeUtils.toIso8601(recording.getStartTime()));
        dto.setUserEmail(recording.getUserEmail());
        dto.setToken(recording.getToken());
        return dto;
    }
}
