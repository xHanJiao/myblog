package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.dto.NameDTO;
import com.xhan.myblog.model.content.repo.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    List<NameDTO> findAllNames();
}
