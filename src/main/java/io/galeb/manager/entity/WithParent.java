package io.galeb.manager.entity;

public interface WithParent<T extends AbstractEntity<?>> {

    T getParent();

}
