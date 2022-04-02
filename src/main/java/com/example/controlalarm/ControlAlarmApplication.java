package com.example.controlalarm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * @author linjiankai
 */
@SpringBootApplication
@EnableKafka
@ComponentScan(basePackages = {"com.example"})
public class ControlAlarmApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlAlarmApplication.class, args);
    }

}
