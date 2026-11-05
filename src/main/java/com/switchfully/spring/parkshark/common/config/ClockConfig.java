package com.switchfully.spring.parkshark.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    /** ParkShark works in Belgian time, whatever time zone the server runs in. */
    public static final ZoneId PARKSHARK_ZONE = ZoneId.of("Europe/Brussels");

    @Bean
    public Clock clock() {
        return Clock.system(PARKSHARK_ZONE);
    }
}
