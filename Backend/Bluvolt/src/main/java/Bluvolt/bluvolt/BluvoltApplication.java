package Bluvolt.bluvolt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class BluvoltApplication {

	public static void main(String[] args) {
		SpringApplication.run(BluvoltApplication.class, args);
	}
	
}
