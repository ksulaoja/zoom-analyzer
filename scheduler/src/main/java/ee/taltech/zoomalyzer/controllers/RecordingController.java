package ee.taltech.zoomalyzer.controllers;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping
    public List<RecordingDto> getRecordings() {
        return recordingService.findAll()
                .stream().map(this::convertToDto)
                .toList();
    }

    @PostMapping
    public RecordingDto scheduleRecording(@RequestBody RecordingDto dto) {
        Recording recording = convertToRecording(dto);
        Recording savedRec = recordingService.save(recording);
        return convertToDto(savedRec);
    }

    @GetMapping("/test")
    public ResponseEntity<Resource> downloadMP4() throws MalformedURLException {
        Path mp4Path = Paths.get("backend/src/main/resources/testfiles/testfile.mp4");

        Resource resource = new UrlResource(mp4Path.toUri());

        if (resource.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=testfile.mp4");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Recording convertToRecording(RecordingDto dto) {
        Recording recording = new Recording();
        recording.setMeetingId(dto.getMeetingId());
        recording.setMeetingPw(dto.getMeetingPw());
        recording.setMeetingUrl(dto.getMeetingUrl());
        recording.setRecordingLength(dto.getRecordingLength());
        recording.setStartTime(TimeUtils.toInstant(dto.getStartTime()));
        recording.setUserEmail(dto.getUserEmail());
        return recording;
    }

    private RecordingDto convertToDto(Recording recording) {
        RecordingDto dto = new RecordingDto();
        dto.setMeetingId(recording.getMeetingId());
        dto.setMeetingPw(recording.getMeetingPw());
        dto.setMeetingUrl(recording.getMeetingUrl());
        dto.setRecordingLength(recording.getRecordingLength());
        dto.setStartTime(TimeUtils.toIso8601(recording.getStartTime()));
        dto.setUserEmail(recording.getUserEmail());
        return dto;
    }
}
