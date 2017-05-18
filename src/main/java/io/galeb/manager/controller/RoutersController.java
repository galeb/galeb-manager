/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.controller;

import com.google.gson.Gson;
import io.galeb.manager.routermap.RouterMapConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/routers", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoutersController {

    private final Gson gson = new Gson();
    private final RouterMapConfiguration.RouterMap routerMap;

    @Autowired
    public RoutersController(RouterMapConfiguration.RouterMap routerMap) {
        this.routerMap = routerMap;
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String routerMap() {
        return gson.toJson(routerMap.get());
    }
}
