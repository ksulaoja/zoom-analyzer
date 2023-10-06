package ee.taltech.zoomalyzer.logging.recorder.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor
public class RecorderLogDto {
    private Long id;
    private String logLevel;
    private String message;
    private String time;

    private Long recordingId;
}
