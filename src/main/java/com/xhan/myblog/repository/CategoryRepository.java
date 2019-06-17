package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.repo.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    void deleteByName(String name);

    long countByName(String name);

    Optional<Category> findByName(String name);
}
