package tn.english.school.retakeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RetakeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetakeServiceApplication.class, args);
    }
}