package io.galeb.manager.entity;

import java.util.Set;

public interface WithParent<T extends AbstractEntity<?>> {

    T getParent();

    Set<T> getChildren();

}
