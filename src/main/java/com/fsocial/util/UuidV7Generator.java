package com.fsocial.util;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.id.uuid.UuidValueGenerator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.util.UUID;

public class UuidV7Generator implements UuidValueGenerator {
    @Override
    public UUID generateUuid(SharedSessionContractImplementor session) {
        return UuidCreator.getTimeOrderedEpoch(); // đây chính là UUID v7
    }
}