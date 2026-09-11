package com.restaurantus.erp.domain;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Column;
import lombok.Data;

@Data
@Document(collection = "language")
public class Language {
    @Id
    @Column(name = "language_code")
    private String languageCode;
    //Prefiero dejarlo en map, ya que si se nos olvida poner un idioma
    //en el documento de mongo , pues de la otra manera a lo mejor peta,
    //y esta manera es más "mongo-friendly" al utilizarte más elementos tipo json
    private Map<String, String> translations;
}
