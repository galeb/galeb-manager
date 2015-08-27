package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Target;

public interface TargetRepositoryCustom {

    Iterable<Target> findAll();

    Page<Target> findAll(Pageable pageable);

    Target findByName(String name);

}
