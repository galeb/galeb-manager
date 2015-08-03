package io.galeb.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import io.galeb.entity.Project;

public interface ProjectRepository extends PagingAndSortingRepository<Project, Long> {

    List<Project> findByName(@Param("name") String name);

}
