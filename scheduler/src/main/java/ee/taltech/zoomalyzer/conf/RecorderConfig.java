package ee.taltech.zoomalyzer.conf;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "recorder")
@Getter @Setter
public class RecorderConfig {
    private String path;
    private String image;
    private int maxJoiningTime; // seconds
    private int statusCheckPeriod; // seconds
    private String fileType;

    public void setMaxJoiningTime(int maxJoiningTime) {
        this.maxJoiningTime = maxJoiningTime * 60;
    }
}
