package ee.taltech.zoomalyzer.controllers.dtos;

import ee.taltech.zoomalyzer.controllers.RecorderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecorderStatusDto {
    private Long meetingInternalId;
    private RecorderStatus.StatusType type;
    private String message;
}
