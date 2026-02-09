package com.webbnba.person_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DateTimeUtil {

    public final Clock clock;

    public Instant now() {
        return clock.instant();
    }
}
