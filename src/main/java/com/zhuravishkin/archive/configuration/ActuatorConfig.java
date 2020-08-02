package com.zhuravishkin.archive.configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ActuatorConfig {
    private MeterRegistry meterRegistry;
    private Counter receivedMessage;
    private Counter sendMessage;
    private Counter lostMessage;
    private static final String CUSTOM_COUNTER_MESSAGE = "custom_counter_message";

    public ActuatorConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initOrderCounters();
    }

    private void initOrderCounters() {
        this.receivedMessage = this.meterRegistry.counter(CUSTOM_COUNTER_MESSAGE, "id", "received");
        this.sendMessage = this.meterRegistry.counter(CUSTOM_COUNTER_MESSAGE, "id", "send");
        this.lostMessage = this.meterRegistry.counter(CUSTOM_COUNTER_MESSAGE, "id", "lost");
    }
}
