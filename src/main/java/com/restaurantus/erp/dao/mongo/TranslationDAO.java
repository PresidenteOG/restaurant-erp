package com.restaurantus.erp.dao.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.restaurantus.erp.domain.TranslationKey;

public interface TranslationDAO extends MongoRepository<TranslationKey, String> {
}
