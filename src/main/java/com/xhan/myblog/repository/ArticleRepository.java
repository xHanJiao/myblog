package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.content.dto.ContentTitleIdDTO;
import com.xhan.myblog.model.content.repo.ArticleHistoryRecord;
import com.xhan.myblog.model.content.repo.CategoryState;
import com.xhan.myblog.model.prj.HistoryRecordsPrj;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<CategoryState> getFirstById(String id);

    @Query(value = "{'historyRecords.recordId': ?0}",
            fields = "{'historyRecords.$': 1}")
    HistoryRecordsPrj getHistoryRecords(String recordId);

    Optional<Article> findByStateAndId(int state, String id);

    int countByCategoryAndState(String category, Integer state);

    int countByCategory(String category);

    int countByState(int state);

    Page<Article> findAllByState(int state, PageRequest pageable);

    Page<Article> findAllByStateAndCategory(int state, String category, PageRequest pageRequest);

    Page<Article> findAllByCategory(String category, PageRequest pageRequest);

}
