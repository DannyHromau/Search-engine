package com.springsearchengine.repositories;

import com.springsearchengine.model.entity.PageData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageDataRepository extends CrudRepository<PageData, Integer> {

    List<PageData> findBySiteId(int siteId);
    PageData deleteBySiteId(int siteId);
}
