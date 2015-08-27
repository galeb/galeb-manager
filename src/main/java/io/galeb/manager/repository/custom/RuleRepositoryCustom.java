package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Rule;

public interface RuleRepositoryCustom {

    Iterable<Rule> findAll();

    Page<Rule> findAll(Pageable pageable);

    Rule findByName(String name);

}
