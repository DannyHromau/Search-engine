package com.springsearchengine.repositories;

import com.springsearchengine.model.entity.Site;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    Site deleteSiteById(int id);
    Site findSiteByUrl(String name);

    Site findSiteById(int id);
}
