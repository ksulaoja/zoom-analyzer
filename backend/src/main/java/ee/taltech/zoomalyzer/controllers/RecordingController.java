package ee.taltech.zoomalyzer.controllers;

import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.services.RecordingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @GetMapping
    public List<Recording> getRecordings() {
        return recordingService.findAll();
    }
}
