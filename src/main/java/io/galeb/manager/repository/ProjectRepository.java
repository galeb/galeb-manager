package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.Project;

@RepositoryRestResource(collectionResourceRel = "project", path = "project")
public interface ProjectRepository extends PagingAndSortingRepository<Project, Long> {

    @Override
    @Query("SELECT p FROM Project p "
            + "INNER JOIN p.teams t "
            + "INNER JOIN t.accounts a "
            + "WHERE p.id = :id")
    Project findOne(@Param("id") Long id);

    @Override
    @Query("SELECT p FROM Project p "
            + "INNER JOIN p.teams t "
            + "INNER JOIN t.accounts a "
            + "WHERE 1=1")
    List<Project> findAll();

    List<Project> findByName(@Param("name") String name);

}
