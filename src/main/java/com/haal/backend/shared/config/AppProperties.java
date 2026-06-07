package com.haal.backend.shared.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "haal")
public class AppProperties {

    private final Jwt jwt = new Jwt();
    private final Safety safety = new Safety();
    private final Geo geo = new Geo();

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
        private long refreshExpirationMs;
    }
    @Data
    public static class Safety {
        private int checkinWindowHours;
        private int alertEscalationHours;
        private String schedulerCron;
    }
    @Data
    public static class Geo {
        private double proximityRadiusKm;
    }

}
