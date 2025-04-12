package com.observeai.platform.realtime.neutrino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableRetry
@EnableAsync
@EnableMongoAuditing
@EnableMongoRepositories
public class NeutrinoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeutrinoApplication.class, args);
    }

}
