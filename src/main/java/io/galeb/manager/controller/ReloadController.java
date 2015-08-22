package io.galeb.manager.controller;

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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value="/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> reload(@PathVariable long id) throws Exception {
        String result = "";
        JsonMapper json = new JsonMapper();
        Farm farm = farmRepository.findOne(id);
        if (farm != null) {
            result = json.makeJson(farm).toString();
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<String>(result, HttpStatus.ACCEPTED);
    }

}
