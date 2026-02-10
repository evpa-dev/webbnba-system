package com.webbnba.person_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @Column(name = "created", nullable = false)
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    private Instant created;

    @NotNull
    @Column(name = "updated", nullable = false)
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    private Instant updated;
}
