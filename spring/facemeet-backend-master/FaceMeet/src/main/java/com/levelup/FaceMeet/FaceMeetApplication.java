package com.levelup.FaceMeet;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableCaching
public class FaceMeetApplication {

	// Static block을 사용하여 애플리케이션 시작 전에 환경변수 로드
	static {
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("./")
					.filename(".env")
					.ignoreIfMissing()
					.load();

			setSystemPropertyIfExists(dotenv, "DB_PASSWORD");
			setSystemPropertyIfExists(dotenv, "JWT_SECRET_KEY");
			setSystemPropertyIfExists(dotenv, "KAKAO_CLIENT_ID");
			setSystemPropertyIfExists(dotenv, "KAKAO_ADMIN_KEY");
			setSystemPropertyIfExists(dotenv, "NAVER_CLIENT_ID");
			setSystemPropertyIfExists(dotenv, "NAVER_CLIENT_SECRET");
			setSystemPropertyIfExists(dotenv, "S3_ACCESS_KEY");
			setSystemPropertyIfExists(dotenv, "S3_SECRET_KEY");


		} catch (Exception e) {
			System.out.println("Failed to load .env file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(FaceMeetApplication.class, args);
	}

	private static void setSystemPropertyIfExists(Dotenv dotenv, String key) {
		String value = dotenv.get(key);
		if (value != null && !value.isEmpty()) {
			System.setProperty(key, value);
		}
	}
}