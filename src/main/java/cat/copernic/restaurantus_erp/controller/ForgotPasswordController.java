package cat.copernic.restaurantus_erp.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.resetpassword.SecurityResetService;
import cat.copernic.restaurantus_erp.services.resetpassword.SecurityResetServiceImpl.VerifyResult;
import cat.copernic.restaurantus_erp.services.user.UserService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // <--- Inyección automática por constructor
public class ForgotPasswordController {

    // Declaramos final para que Lombok los inyecte
    private final SecurityResetService securityService;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "forgot_password/request_code";
    }

    @PostMapping("/forgot-password")
    public String processForgotRequest(@RequestParam String username, @RequestParam String phoneNumber, RedirectAttributes redirectAttributes, Locale locale) {
        
        // 1. Validar reglas unificadas (Usuario mal, Teléfono mal, o Sin Telegram)
        String errorKey = securityService.validateUserForReset(username, phoneNumber);

        if (errorKey != null) {
            // CAMBIO: Ponemos el texto directo aquí para evitar errores de "NoSuchMessageException"
            // y no depender del messages.properties
            String errorMsg = "Usuario o teléfono incorrectos, o cuenta de Telegram no vinculada.";
            
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return "redirect:/forgot-password";
        }

        // 2. Si todo está perfecto, iniciamos el envío
        boolean sent = securityService.initiateReset(username, phoneNumber);
        
        if (sent) {
            return "redirect:/verify-code?username=" + username;
        } else {
            // Error técnico genérico
            redirectAttributes.addFlashAttribute("error", "Error al enviar el mensaje. Inténtalo más tarde.");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/verify-code")
    public String showVerifyPage(@RequestParam(required = false) String username, Model model) {
        if (username == null || !securityService.hasPendingRequest(username)) {
            return "redirect:/forgot-password";
        }
        model.addAttribute("username", username);
        model.addAttribute("waitTime", securityService.getWaitTime(username));
        return "forgot_password/verify_code";
    }

    @GetMapping("/resend-code")
    public String resendCode(@RequestParam String username, RedirectAttributes redirectAttributes, Locale locale) {
        if (securityService.resendCode(username)) {
            String infoMsg = messageSource.getMessage("forgot_pwd.info.new_code_sent", null, locale);
            redirectAttributes.addFlashAttribute("info", infoMsg);
        }
        return "redirect:/verify-code?username=" + username;
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String username, @RequestParam String code, Model model, Locale locale) {
        VerifyResult result = securityService.verifyCode(username, code);
        String errorMsg;

        switch (result) {
            case SUCCESS:
                model.addAttribute("username", username);
                return "forgot_password/change_password";
            case LOCKED:
                errorMsg = "⛔ " + messageSource.getMessage("forgot_pwd.error.account_blocked", null, locale);
                model.addAttribute("error", errorMsg);
                return "login";
            case INVALID:
                model.addAttribute("username", username);
                errorMsg = messageSource.getMessage("forgot_pwd.error.wrong_code", null, locale);
                model.addAttribute("error", errorMsg);
                model.addAttribute("waitTime", securityService.getWaitTime(username)); 
                return "forgot_password/verify_code"; 
            default:
                return "redirect:/forgot-password?error=expired";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        User user = this.userService.findUserByName(username);
        if(user != null) {
            this.userService.updateUserPassword(user.getId(), newPassword, newPassword);
        }
        return "redirect:/login?resetSuccess=true";
    }
}