package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.ContentTitleIdDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<Article> findByPublishedAndFinishedAndId(Boolean published, Boolean finished, String id);

    Page<Article> findByPublishedAndFinished(Boolean published, Boolean finished, PageRequest pageable);

    int countByCategoryAndPublishedAndFinished(String category, Boolean published, Boolean finished);

    int countByCategory(String category);

    int countByPublishedAndFinished(Boolean published, Boolean finished);

    Page<Article> findAllByPublishedAndFinished(Boolean published, Boolean finished, PageRequest pageable);

    Page<Article> findAllByPublishedAndFinishedAndCategory(Boolean published, Boolean finished, String category, PageRequest pageRequest);

    Page<Article> findAllByCategory(String category, PageRequest pageRequest);

    Page<Article> findAllByPublished(Boolean published, PageRequest request);

    Page<Article> findAllByFinished(Boolean finished, PageRequest request);

    Optional<ContentTitleIdDTO> getById(String id);

}
