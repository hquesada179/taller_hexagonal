package com.stagepass.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StagePassLiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(StagePassLiveApplication.class, args);
    }
}
