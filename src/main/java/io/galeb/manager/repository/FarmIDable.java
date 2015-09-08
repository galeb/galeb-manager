package io.galeb.manager.repository;

public interface FarmIDable<T> {

    Iterable<T> findByFarmId(long id);

}
