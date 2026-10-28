package cr.ac.ucr.sim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimApplication.class, args);
    }

}
