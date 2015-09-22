package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Target;

public interface TargetRepositoryCustom {

    Page<Target> findAll(Pageable pageable);

    Page<Target> findByName(String name, Pageable pageable);

}
