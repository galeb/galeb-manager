package io.galeb.manager.repository;

import java.util.List;

public interface FarmIDable<T> {

    List<T> findByFarmId(long id);

}
