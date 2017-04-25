package io.galeb.manager.repository.custom;

import io.galeb.manager.entity.Target;

import java.util.List;

public interface TargetRepositoryCustom {

    List<Target> allAvaliablesOf(String pool);
}
