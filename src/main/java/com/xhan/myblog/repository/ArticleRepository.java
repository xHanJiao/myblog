package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.ContentTitleIdDTO;
import com.xhan.myblog.model.content.IdTitleTimeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

//    Optional<CertainArticleDTO> findByDeletedAndFinishedAndId(Boolean deleted, String id);

    Optional<Article> findByDeletedAndFinishedAndId(Boolean deleted, Boolean finished, String id);

    Page<Article> findByDeletedAndFinished(Boolean deleted, Boolean finished, Pageable pageable);

    Page<Article> findAllByDeletedAndFinished(Boolean deleted, Boolean finished, Pageable pageable);

    Optional<ContentTitleIdDTO> getById(String id);

}
