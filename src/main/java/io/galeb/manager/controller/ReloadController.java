package io.galeb.manager.controller;

import static io.galeb.manager.entity.AbstractEntity.EntityStatus.PENDING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.galeb.manager.common.JsonMapper;
import io.galeb.manager.engine.farm.FarmEngine;
import io.galeb.manager.entity.Farm;
import io.galeb.manager.handler.RoutableToEngine;
import io.galeb.manager.repository.FarmRepository;

@RestController
@RequestMapping(value="/reload")
public class ReloadController extends RoutableToEngine<Farm> {

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private JmsTemplate jms;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> reload(@PathVariable long id) throws Exception {
        String result;
        JsonMapper json = new JsonMapper();
        Farm farm = farmRepository.findOne(id);
        if (farm != null) {
            farm.setStatus(PENDING).setSaveOnly(true);
            farmRepository.save(farm);
            jmsSend(jms, FarmEngine.QUEUE_RELOAD, farm);
            result = json.putString("farm", farm.getName()).putString("status", "accept").toString();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
    }

    @Override
    protected void setBestFarm(Farm entity) throws Exception {
        //
    }

}
