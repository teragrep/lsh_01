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
package com.teragrep.lsh_01.authentication;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class BasicAuthenticationFactory {

    public BasicAuthentication create() {
        BufferedReader br;
        String credentialsFile = System.getProperty("credentials.file", "etc/credentials.json");
        try {
            br = new BufferedReader(new FileReader(credentialsFile));
        }
        catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "Can't find credentials.json from path <[" + credentialsFile + "]>: ",
                    e
            );
        }
        return new BasicAuthentication(br);
    }
}
