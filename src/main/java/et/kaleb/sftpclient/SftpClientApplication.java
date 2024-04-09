package et.kaleb.sftpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SftpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpClientApplication.class, args);
	}

}
