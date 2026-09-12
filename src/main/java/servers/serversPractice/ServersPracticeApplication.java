package servers.serversPractice;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServersPracticeApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ServersPracticeApplication.class, args);
	}

	@Override
	public void run(String @NonNull ... args) {
		System.out.println("Application started!");
		System.out.println("Hello from CommandLineRunner!");
	}
}