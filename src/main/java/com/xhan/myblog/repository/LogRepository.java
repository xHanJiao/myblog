package com.xhan.myblog.repository;

import com.xhan.myblog.model.content.repo.MongoLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<MongoLog, String> {
}
