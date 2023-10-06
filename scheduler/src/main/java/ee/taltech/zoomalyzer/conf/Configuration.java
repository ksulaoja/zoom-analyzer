package ee.taltech.zoomalyzer.conf;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import ee.taltech.zoomalyzer.logging.general.LogLevel;
import ee.taltech.zoomalyzer.logging.general.LogLevelDal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.List;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    CommandLineRunner commandLineRunner(RecordingDal recordingDal, LogLevelDal logLevelDal) {
        return env -> {
            Recording recording = new Recording();
            recording.setRecordingLength(5);
            recording.setStartTime(Instant.now().plusSeconds(30L));
            recording.setMeetingUrl("https://zoom.com");
            recording.setUserEmail("paulbryantan@gmail.com");
            recordingDal.save(recording);

            LogLevel info = new LogLevel("INFO");
            LogLevel debug = new LogLevel("DEBUG");
            LogLevel error = new LogLevel("ERROR");
            LogLevel status = new LogLevel("STATUS");
            logLevelDal.saveAll(List.of(info, debug, error, status));
        };
    }
}
