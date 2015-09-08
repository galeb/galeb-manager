package io.galeb.manager.entity;

public interface WithFarmID<T extends AbstractEntity<?>> {

    public long getFarmId();

    public T setFarmId(long farmId);

}
