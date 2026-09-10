package cat.copernic.restaurantus_erp.config;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import cat.copernic.restaurantus_erp.services.LangService;
import jakarta.annotation.PostConstruct;

//el albert no lo ha explicado pero el messagesource es una cosa que viene
//con el springboot y hace que no haga falta poner cada traduccion como un
//atributo en el controller, y ya se añade directamente al thymeleaf
//
//Las traducciones "de verdad" viven en MongoDB. Cuando una clave no está en
//Mongo -o Mongo no está disponible, p. ej. en el perfil demo- se cae hacia el
//bundle classpath:messages(.properties), un juego completo en inglés generado
//a partir de las claves de las plantillas.
@Component("messageSource")
public class MongoMessageSource extends AbstractMessageSource {
    @Autowired
    private LangService translationService;

    @PostConstruct
    public void initFallback() {
        ResourceBundleMessageSource fallback = new ResourceBundleMessageSource();
        fallback.setBasename("messages");
        fallback.setDefaultEncoding(StandardCharsets.UTF_8.name());
        fallback.setUseCodeAsDefaultMessage(false);
        setParentMessageSource(fallback);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = safeTranslation(code, locale.getLanguage());

        //devolver null para que spring consulte el parent (bundle classpath)
        if (message == null || message.equals(code)) {
            return null;
        }

        return new MessageFormat(message, locale);
    }

    private String safeTranslation(String code, String language) {
        try {
            return this.translationService.getTranslation(code, language);
        } catch (RuntimeException ex) {
            //Mongo caído o sin datos: que resuelva el bundle de fallback
            return null;
        }
    }
}
