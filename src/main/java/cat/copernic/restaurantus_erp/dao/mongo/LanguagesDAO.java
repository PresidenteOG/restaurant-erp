package cat.copernic.restaurantus_erp.dao.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import cat.copernic.restaurantus_erp.domain.Language;

public interface LanguagesDAO extends MongoRepository<Language, String> {
}
