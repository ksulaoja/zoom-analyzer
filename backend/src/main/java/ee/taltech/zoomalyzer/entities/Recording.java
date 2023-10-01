package ee.taltech.zoomalyzer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String recordingPath;
    private String meetingId;
    private String meetingPw;
    private String meetingUrl;
    private Instant startTime;
    private int recordingLength;


    public Recording(Long id, String recordingPath) {
        this.id = id;
        this.recordingPath = recordingPath;
    }

    public Recording() {
    }

    public String getPath() {
        return recordingPath;
    }

    public void setPath(String path) {
        this.recordingPath = path;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
