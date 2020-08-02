package com.zhuravishkin.archive.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Receiver {
    public void receiveMessage(String message) {
        log.info("Received from RabbitMQ: " + message);
    }
}
