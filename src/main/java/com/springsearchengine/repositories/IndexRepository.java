package com.springsearchengine.repositories;

import com.springsearchengine.model.entity.Index;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
    Index findByLemmaIdAndPageDataId(int lemmaId, int pageDataId);
}
