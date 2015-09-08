package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Project;

public interface ProjectRepositoryCustom {

    Page<Project> findAll(Pageable pageable);

    Project findByName(String name);
}
