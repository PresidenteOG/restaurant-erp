package cat.copernic.restaurantus_erp.controller; 
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute; 

import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.user.UserService;


@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired 
    private UserService userService;

    @ModelAttribute("currentUser")
     public User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userService.findByUsernameVIEW(principal.getName());
    }

    @ModelAttribute("faviconPath")
    public String getFaviconPath() {
        return "/images/favicon.png";
    }

    @ModelAttribute("appName")
    public String getAppName() {
        return "Restaurantus";
    }
}   
