package io.galeb.manager.controller;

import javax.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isFullyAuthenticated()")
    public String token(HttpSession session) {
        return "{\"token\":\""+session.getId()+"\"}";
    }

}
