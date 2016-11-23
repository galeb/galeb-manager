/*
 * Galeb - Load Balance as a Service Plataform
 *
 * Copyright (C) 2014-2016 Globo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.controller;

import io.galeb.manager.common.JsonMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.lang.management.ManagementFactory;

@RestController
@RequestMapping(value="/info")
public class InfoController {

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> info() throws Exception {
        String result;
        JsonMapper json = new JsonMapper();

        long uptimeJVM = ManagementFactory.getRuntimeMXBean().getUptime();
        String uptime = getUptimeCommand();
        String version = getClass().getPackage().getImplementationVersion();

        result = json.putString("uptime", uptime).putString("uptime-jvm", String.valueOf(uptimeJVM)).putString("version", version).toString();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    private String getUptimeCommand() {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "uptime");
        processBuilder.redirectErrorStream(true);
        try {
            Process process = processBuilder.start();
            InputStream stream = process.getInputStream();
            return convertStreamToString(stream).replace("\n", "");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {

        if (is != null) {
            final Writer writer = new StringWriter();

            final char[] buffer = new char[1024];
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
        return ""; //$NON-NLS-1$
    }


}
