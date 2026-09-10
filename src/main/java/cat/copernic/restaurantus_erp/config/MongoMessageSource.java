package cat.copernic.restaurantus_erp.config;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import cat.copernic.restaurantus_erp.services.LangService;

//el albert no lo ha explicado pero el messagesource es una cosa que viene
//con el springboot y hace que no haga falta poner cada traduccion como un
//atributo en el controller, y ya se añade directamente al thymeleaf
@Component("messageSource")
public class MongoMessageSource extends AbstractMessageSource {
    @Autowired
    private LangService translationService;

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = this.translationService.getTranslation(code, locale.getLanguage());

        //devolver null para que el spring intente usar el default
        if (message == null || message.equals(code)) {
            return null; 
        }

        return new MessageFormat(message, locale);
    }
}
