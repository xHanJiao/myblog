package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.dto.ContentTitleIdDTO;
import com.xhan.myblog.model.content.repo.CategoryState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<CategoryState> getFirstById(String id);

    Optional<Article> findByStateAndId(int state, String id);

    Page<Article> findByState(int state, PageRequest pageable);

    int countByCategoryAndState(String category, Integer state);

    int countByCategory(String category);

    int countByState(int state);

    Page<Article> findAllByStateIn(Collection<Integer> integers, PageRequest pageRequest);

    Page<Article> findAllByState(int state, PageRequest pageable);

    Page<Article> findAllByStateAndCategory(int state, String category, PageRequest pageRequest);

    Page<Article> findAllByCategory(String category, PageRequest pageRequest);

    Optional<ContentTitleIdDTO> getById(String id);

    void deleteByTitleAndState(String title, int state);

}
