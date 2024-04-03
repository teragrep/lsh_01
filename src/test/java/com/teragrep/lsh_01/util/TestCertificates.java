/*
  logstash-http-input to syslog bridge
  Copyright 2024 Suomen Kanuuna Oy

  Derivative Work of Elasticsearch
  Copyright 2012-2015 Elasticsearch

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
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
