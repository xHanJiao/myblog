package com.xhan.myblog.repository;

import com.xhan.myblog.model.user.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface AdminRepository extends MongoRepository<Admin, String> {
}
