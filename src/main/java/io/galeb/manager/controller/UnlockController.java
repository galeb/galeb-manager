package io.galeb.manager.controller;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.engine.service.LockerManager;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.queue.FarmQueue;
import io.galeb.manager.repository.FarmRepository;
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

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;

@RestController
@RequestMapping(value="/unlock")
public class UnlockController {

    private static final Log LOGGER = LogFactory.getLog(UnlockController.class);
    private final LockerManager lockerManager = new LockerManager();

    @Autowired private FarmRepository farmRepository;
    @Autowired private FarmQueue farmQueue;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> reload(@PathVariable long id) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LOGGER.warn("Unlock Farm called by " + authentication.getName());
        String result;
        JsonMapper json = new JsonMapper();
        Farm farm = farmRepository.findOne(id);
        if (farm != null) {
            String[] apis = farm.getApi().split(",");

            lockerManager.release(farm.idName(),apis);

            result = json.putString("farm", farm.getName()).putString("status", "accept").toString();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

}
