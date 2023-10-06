package ee.taltech.zoomalyzer.entities;

import ee.taltech.zoomalyzer.util.TimeUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String filePath;

    // TODO add validations that either meetingId and password OR meetingURL is present
    private String meetingId;
    private String meetingUrl;
    private String meetingPw;

    @NotNull(message = "Start time has to be set.")
    // TODO validate that start time is in the future
    private Instant startTime;

    @NotNull(message = "Recording length has to be set.")
    @Min(value = 1, message = "Recording length must be greater than 1 minute.")
    @Max(value = 45, message = "Recording length must be less than 45 minutes.")
    private int recordingLength;

    @Email @NotBlank
    private String userEmail;
    private boolean scheduled = false;

    public Recording() {
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String path) {
        this.filePath = path;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public String getMeetingPw() {
        return meetingPw;
    }

    public void setMeetingPw(String meetingPw) {
        this.meetingPw = meetingPw;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public int getRecordingLength() {
        return recordingLength;
    }

    public void setRecordingLength(int recordingLength) {
        this.recordingLength = recordingLength;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    @Override
    public String toString() {
        return "Recording{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                ", meetingId='" + meetingId + '\'' +
                ", meetingUrl='" + meetingUrl + '\'' +
                ", meetingPw='" + meetingPw + '\'' +
                ", startTime=" + TimeUtils.toIso8601(startTime) +
                ", recordingLength=" + recordingLength +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
