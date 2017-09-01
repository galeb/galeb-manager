package io.galeb.manager.entity;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public interface WithFarmID<T extends AbstractEntity<?>> {

    long getFarmId();

    T setFarmId(long farmId);

    default Farm getFarm() {
        throw new NotImplementedException();
    }

}
