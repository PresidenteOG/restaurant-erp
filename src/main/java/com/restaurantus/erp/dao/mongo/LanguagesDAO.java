package com.restaurantus.erp.dao.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.restaurantus.erp.domain.Language;

public interface LanguagesDAO extends MongoRepository<Language, String> {
}
