package com.haal.backend.safety.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "check_ins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "checked_in_at", nullable = false)
    private Instant checkedInAt;

    // PostGIS Point — nullable (location is optional for check-in)
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "alert_sent", nullable = false)
    @Builder.Default
    private boolean alertSent = false;

    @Column(name = "alert_sent_at")
    private Instant alertSentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}