package io.galeb.manager.repository.custom;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import io.galeb.manager.entity.Rule;

public interface RuleRepositoryCustom {

    Rule save(Rule rule) throws ResourceNotFoundException;

}
