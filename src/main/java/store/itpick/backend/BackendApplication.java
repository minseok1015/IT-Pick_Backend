package store.itpick.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	@PostConstruct
	public void started(){
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));	// EC2에서도 Tomcat 서버의 시간을 서울 시간으로 변경한다.

	}

	public static void main(String[] args) {
		//test
		SpringApplication.run(BackendApplication.class, args);
	}

}
