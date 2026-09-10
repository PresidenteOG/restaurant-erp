package cat.copernic.restaurantus_erp.services;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cat.copernic.restaurantus_erp.dao.mongo.LanguagesDAO;
import cat.copernic.restaurantus_erp.dao.mongo.TranslationDAO;
import jakarta.annotation.PostConstruct;

@Service
public class LangServiceImpl implements LangService {
    @Autowired
    private TranslationDAO idiomesDAO;
    @Autowired
    private LanguagesDAO newIdiomesDAO;
    // Cache local: Map<Idioma, Map<Clave, Valor>>
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void loadTranslations() {
        //Este codigo lo que hace es basicamente añadir cada translation key que
        //se haya encontrado a un idioma, pasando de
        //haber "cada key individual con su version en todos los idiomas"
        //a ser "cada idioma tiene su mapa de key y valor (un solo idioma)"
        //unicamente en el map de cache
        /*long count = this.idiomesDAO.count();
        System.out.println("Documentos encontrados en Mongo: " + count);*/
        this.idiomesDAO.findAll().forEach(tKey -> {
            //System.out.println("Cargando clave: " + tKey.getKey());
            tKey.getTranslations().forEach((lang, value) -> {
                this.cache.computeIfAbsent(lang, k -> new ConcurrentHashMap<>())
                     .put(tKey.getKey(), value);
            });
        });
        this.newIdiomesDAO.findAll().forEach(lang -> {
            Map<String, String> map = this.cache.computeIfAbsent(lang.getLanguageCode(), k -> new ConcurrentHashMap<>());
            lang.getTranslations().forEach((tKey, value) -> {
                map.put(tKey, value);
            });
        });
    }

    public String getTranslation(String key, String idioma) {
        //si no encuentra el idioma, devuelve un empty collection,
        //y si no existe la clave devuelce la propia clave
        return this.cache.getOrDefault(idioma, Collections.emptyMap()).getOrDefault(key, key);
    }
}