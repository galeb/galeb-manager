package io.galeb.manager.entity;

import java.util.Set;

public interface WithParents<T extends AbstractEntity<?>> {

    Set<T> getParents();

}
