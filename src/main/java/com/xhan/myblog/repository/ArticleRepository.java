package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.repo.Article;
import com.xhan.myblog.model.prj.CategoryState;
import com.xhan.myblog.model.prj.HistoryRecordsPrj;
import com.xhan.myblog.model.prj.IdTitleTimeStatePrj;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends MongoRepository<Article, String> {

    Optional<CategoryState> getFirstById(String id);

    @Query(value = "{'historyRecords.recordId': ?0}",
            fields = "{'historyRecords.$': 1}")
    HistoryRecordsPrj getHistoryRecords(String recordId);

    Optional<Article> findByIdAndState(String id, int state);

    Page<Article> getAllBy(PageRequest pageRequest);

    Page<Article> getAllByState(int state, PageRequest pageRequest);

    Page<IdTitleTimeStatePrj> findAllBy(PageRequest pageRequest);

    Page<IdTitleTimeStatePrj> findAllByState(int state, PageRequest pageable);

    Page<IdTitleTimeStatePrj> findAllByStateAndCategory(int state, String category, PageRequest pageRequest);

    Page<IdTitleTimeStatePrj> findAllByCategory(String category, PageRequest pageRequest);

    List<IdTitleTimeStatePrj> findByTitleRegex(String titleRegex);

    List<IdTitleTimeStatePrj> findByTitleRegexAndState(String titleRegex, int state);

    int countByCategoryAndState(String category, Integer state);

    int countByCategory(String category);

    int countByState(int state);
}
