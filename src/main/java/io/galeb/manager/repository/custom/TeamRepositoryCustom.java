package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Team;

public interface TeamRepositoryCustom {

    Page<Team> findAll(Pageable pageable);

    Page<Team> findByName(String name, Pageable pageable);

}
