package com.chargev.emsp.entity.idgen;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class IdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o)
            throws HibernateException {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
