package io.galeb.manager.controller;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;
import static java.util.AbstractMap.*;

import io.galeb.manager.jms.FarmQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private FarmQueue farmQueue;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> reload(@PathVariable long id) throws Exception {
        String result;
        JsonMapper json = new JsonMapper();
        Farm farm = farmRepository.findOne(id);
        if (farm != null) {
            farm.setStatus(PENDING).setSaveOnly(true);
            farmRepository.save(farm);
            farmQueue.sendToQueue(FarmQueue.QUEUE_RELOAD, new SimpleImmutableEntry<>(farm, null));
            result = json.putString("farm", farm.getName()).putString("status", "accept").toString();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

}
