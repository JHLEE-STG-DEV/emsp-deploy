package com.chargev.emsp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;

@Configuration
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

    @Override
    public String getConnectionString() {
        return "couchbase://10.200.110.203,10.200.111.92";
    }

    @Override
    public String getUserName() {
        return "csmsap";
    }

    @Override
    public String getPassword() {
        return "ckwlql!23";
    }

    @Override
    public String getBucketName() {
        return "evcp-status";
    }
}