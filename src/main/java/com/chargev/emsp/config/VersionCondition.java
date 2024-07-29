package com.chargev.emsp.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

public class VersionCondition implements Condition {
    private final String expectedVersion;

    public VersionCondition(String expectedVersion) {
        this.expectedVersion = expectedVersion;
    }

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String version = context.getEnvironment().getProperty("version");
        return expectedVersion.equalsIgnoreCase(version);
    }
}
