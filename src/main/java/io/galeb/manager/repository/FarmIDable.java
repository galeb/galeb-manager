package io.galeb.manager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FarmIDable<T> {

    Page<T> findByFarmId(long id, Pageable pageable);

}
