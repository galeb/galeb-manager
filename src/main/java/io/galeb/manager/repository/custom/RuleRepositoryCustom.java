package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Rule;

public interface RuleRepositoryCustom {

    Page<Rule> findAll(Pageable pageable);

    Page<Rule> findByName(String name, Pageable pageable);

}
