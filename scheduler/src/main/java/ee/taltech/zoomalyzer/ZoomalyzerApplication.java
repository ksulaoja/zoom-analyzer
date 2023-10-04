package ee.taltech.zoomalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZoomalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoomalyzerApplication.class, args);
	}

}
