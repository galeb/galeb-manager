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

import io.galeb.manager.common.ErrorLogger;
import io.galeb.manager.entity.service.EtagService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(value="/virtualhostscached", produces = MediaType.APPLICATION_JSON_VALUE)
public class VirtualHostsCachedController {

    private static final Log LOGGER = LogFactory.getLog(VirtualHostsCachedController.class);

    private final EtagService etagService;

    @Autowired
    public VirtualHostsCachedController(EtagService etagService) {
        this.etagService = etagService;
    }

    @RequestMapping(value="/{envname:.+}", method = RequestMethod.GET)
    public synchronized ResponseEntity showall(@PathVariable String envname,
                                  @RequestHeader(value = "If-None-Match", required = false) String routerEtag,
                                  @RequestHeader(value = "X-Galeb-GroupID", required = false) String routerGroupId) throws Exception {
        return buildResponse(envname, routerGroupId, routerEtag);
    }

    @Transactional
    private ResponseEntity buildResponse(String envname, String routerGroupId, String routerEtag) throws Exception {
        Assert.notNull(envname, "Environment name is null");
        Assert.notNull(routerGroupId, "GroupID undefined");
        Assert.notNull(routerEtag, "etag undefined");

        try {
            String responseBody = etagService.responseBody(envname, routerGroupId, routerEtag);
            if (HttpStatus.NOT_MODIFIED.name().equals(responseBody)) {
                LOGGER.warn("If-None-Match header matchs with internal etag, then ignoring request");
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            if (HttpStatus.NOT_FOUND.name().equals(responseBody)) {
                LOGGER.warn("responseBody is empty");
                throw new VirtualHostsEmptyException();
            }
            return new ResponseEntity<>(responseBody, OK);
        } catch (VirtualHostsEmptyException virtualHostsEmptyException) {
            throw virtualHostsEmptyException;
        } catch (Exception e) {
            ErrorLogger.logError(e, this.getClass());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ResponseStatus(value= HttpStatus.NOT_FOUND, reason = "Virtualhosts empty in this environment")
    private static class VirtualHostsEmptyException extends Exception
    {
        private static final long serialVersionUID = 1L;
    }
}
