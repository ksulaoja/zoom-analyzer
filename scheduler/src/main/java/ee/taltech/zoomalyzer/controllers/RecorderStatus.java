package ee.taltech.zoomalyzer.controllers;

import ee.taltech.zoomalyzer.controllers.dtos.RecorderStatusDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/recorder")
public class RecorderStatus {
    private static final Logger logger = Logger.getLogger(RecorderStatus.class.getSimpleName());

    public enum StatusType {
        OK, FAIL
    }

    @PostMapping("/status")
    public void sendStatusUpdate(@RequestBody RecorderStatusDto statusDto) {
        String msg = String.format("Recording id %s status update: %s",
                statusDto.getMeetingInternalId(), statusDto.getMessage());
        if (statusDto.getType().equals(StatusType.OK)) {
            logger.info(msg);
        } else {
            logger.warning(msg);
        }
    }
}
