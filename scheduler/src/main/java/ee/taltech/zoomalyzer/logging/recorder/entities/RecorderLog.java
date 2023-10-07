package ee.taltech.zoomalyzer.logging.recorder.entities;

import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.general.LogLevel;
import ee.taltech.zoomalyzer.util.TimeUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.Instant;

@Entity
public class RecorderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "log_level_id")
    private LogLevel logLevel;
    private String message;
    private Instant time;

    @ManyToOne
    @JoinColumn(name = "recording_id")
    private Recording recording;

    public RecorderLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Recording getRecording() {
        return recording;
    }

    public void setRecording(Recording recording) {
        this.recording = recording;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "RecorderLog{" +
                "id=" + id +
                ", logLevel=" + logLevel +
                ", message='" + message + '\'' +
                ", time=" + TimeUtils.toIso8601(time) +
                ", recording=" + recording +
                '}';
    }
}
