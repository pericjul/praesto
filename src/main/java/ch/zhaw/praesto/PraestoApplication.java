package ch.zhaw.praesto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// UserDetailsServiceAutoConfiguration ausgeschlossen: wir authentifizieren ausschliesslich
// per JWT (siehe SecurityConfig). Das verhindert den von Spring generierten Default-User samt
// Warnung "Using generated security password ..." – nichts davon gehört in eine produktive App.
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@EnableScheduling
public class PraestoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PraestoApplication.class, args);
	}

}
