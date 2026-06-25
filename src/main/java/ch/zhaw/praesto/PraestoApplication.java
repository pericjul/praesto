package ch.zhaw.praesto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PraestoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraestoApplication.class, args);
	}

}
