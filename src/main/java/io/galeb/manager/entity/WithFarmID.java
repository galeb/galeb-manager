package io.galeb.manager.entity;

public interface WithFarmID<T extends AbstractEntity<?>> {

    long getFarmId();

    T setFarmId(long farmId);

    Farm getFarm();

}
