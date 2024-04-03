package com.teragrep.lsh_01.util;

interface TestCertificates {
    String CERTIFICATE = TestUtils.resourcePath("certificates/host.crt");
    String KEY = TestUtils.resourcePath("certificates/host.key");
    String KEY_ENCRYPTED = TestUtils.resourcePath("certificates/host.enc.key");
    String KEY_ENCRYPTED_PASS = "changeme";
    String CA = TestUtils.resourcePath("certificates/root-ca.crt");


    String KEYSTORE = TestUtils.resourcePath("certificates/host-keystore.p12");
    String KEYSTORE_TYPE = "PKCS12";
    String KEYSTORE_PASSWORD = "changeme";


    String TRUSTSTORE = TestUtils.resourcePath("certificates/truststore.jks");
    String TRUSTSTORE_TYPE = "jks";
    String TRUSTSTORE_PASSWORD = "changeme";
}
