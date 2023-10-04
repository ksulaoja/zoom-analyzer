package ee.taltech.zoomalyzer.conf;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.Instant;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    CommandLineRunner commandLineRunner(RecordingDal recordingDal) {
        return env -> {
            Recording recording = new Recording();
            recording.setRecordingLength(5);
            recording.setStartTime(Instant.now().plusSeconds(30L));
            recording.setMeetingUrl("https://zoom.com");
            recording.setUserEmail("paulbryantan@gmail.com");
            recordingDal.save(recording);
        };
    }
}