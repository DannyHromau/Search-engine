package com.springsearchengine.repositories;

import com.springsearchengine.model.entity.Lemma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
     List<Lemma> findByLemma(String lemma);
     Lemma deleteLemmaBySiteId(int siteId);
     List<Lemma> findLemmaBySiteId(int siteId);
}
