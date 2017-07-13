package io.galeb.manager.entity;

public interface WithFarmID<T extends AbstractEntity<?>> {

    long getFarmId();

    T setFarmId(long farmId);

    default Farm getFarm() {
        return getFakeFarm();
    }

    default Farm getFakeFarm() {
        return new Farm().setName("fake").setAutoReload(false);
    }

}
