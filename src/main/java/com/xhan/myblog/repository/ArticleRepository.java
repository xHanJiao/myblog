package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.Article;
import com.xhan.myblog.model.content.IdTitleTimeDTO;
import com.xhan.myblog.model.content.TitleContentCommTimeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

//    Optional<TitleContentCommTimeDTO> findByDeletedAndId(Boolean deleted, String id);

    Optional<TitleContentCommTimeDTO> findByDeletedAndId(Boolean deleted, String id);

    Page<IdTitleTimeDTO> findAllByDeleted(Boolean deleted, Pageable pageable);

}
