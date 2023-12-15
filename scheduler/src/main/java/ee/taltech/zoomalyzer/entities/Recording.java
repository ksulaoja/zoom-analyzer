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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO add validations that either meetingId and password OR meetingURL is present
    private String meetingId;
    private String meetingPw;

    @NotNull(message = "Start time has to be set.")
    // TODO validate that start time is in the future
    private Instant startTime;

    @NotNull(message = "Recording duration has to be set.")
    @Min(value = 1, message = "Recording duration must be greater than 1 minute.")
    @Max(value = 90, message = "Recording duration must be less than 90 minutes.")
    private int duration; // minutes

    @Email @NotBlank
    private String userEmail;
    private boolean scheduled = false;
    private String token;

    public Recording() {
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int recordingLength) {
        this.duration = recordingLength;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Recording{" +
                "id=" + id +
                ", meetingId='" + meetingId + '\'' +
                ", meetingPw='" + meetingPw + '\'' +
                ", startTime=" + TimeUtils.toIso8601(startTime) +
                ", recordingLength=" + duration +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
