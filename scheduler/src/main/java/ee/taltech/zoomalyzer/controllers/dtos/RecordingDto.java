package ee.taltech.zoomalyzer.controllers.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecordingDto {
    private Long id;
    private String meetingId;
    private String meetingUrl;
    private String meetingPw;
    private String startTime; // iso8601DateTime "2023-10-04T14:30:00.000Z" UTC
    private int recordingLength;
    private String userEmail;
}
