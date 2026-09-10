package cat.copernic.restaurantus_erp.dao.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import cat.copernic.restaurantus_erp.domain.TranslationKey;

public interface TranslationDAO extends MongoRepository<TranslationKey, String> {
}
