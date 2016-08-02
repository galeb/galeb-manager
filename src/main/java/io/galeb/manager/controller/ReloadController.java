package io.galeb.manager.controller;

import io.galeb.manager.cache.DistMap;
import io.galeb.manager.queue.FarmQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.repository.FarmRepository;

@RestController
@RequestMapping(value="/reload")
public class ReloadController {

    private static final Log LOGGER = LogFactory.getLog(ReloadController.class);

    @Autowired private FarmRepository farmRepository;
    @Autowired private FarmQueue farmQueue;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> reload(@PathVariable long id) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LOGGER.warn("Full Farm Reload called by " + authentication.getName());
        String result;
        JsonMapper json = new JsonMapper();
        Farm farm = farmRepository.findOne(id);
        if (farm != null) {
            farmQueue.sendToQueue(FarmQueue.QUEUE_RELOAD, farm);
            result = json.putString("farm", farm.getName()).putString("status", "accept").toString();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

}
