package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.CertainArticleDTO;
import com.xhan.myblog.model.content.IdTitleTimeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

//    Optional<CertainArticleDTO> findByDeletedAndId(Boolean deleted, String id);

    Optional<CertainArticleDTO> findByDeletedAndId(Boolean deleted, String id);

    Page<IdTitleTimeDTO> findAllByDeleted(Boolean deleted, Pageable pageable);

}
