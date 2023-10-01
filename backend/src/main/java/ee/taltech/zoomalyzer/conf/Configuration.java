package ee.taltech.zoomalyzer.conf;

import ee.taltech.zoomalyzer.dal.RecordingDal;
import ee.taltech.zoomalyzer.entities.Recording;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    CommandLineRunner commandLineRunner(RecordingDal recordingDal) {
        return env -> {
            Recording recording = new Recording(null, "example/path/to/file");
            recordingDal.save(recording);
        };
    }
}
