package cat.copernic.restaurantus_erp.services.resetpassword;

import cat.copernic.restaurantus_erp.services.resetpassword.SecurityResetServiceImpl.VerifyResult;

public interface SecurityResetService {

    // Valida si el usuario cumple los requisitos (existe, teléfono coincide, tiene Telegram)
    // Retorna NULL si todo está bien, o el String de la clave del error si falla.
    String validateUserForReset(String username, String phoneNumber);

    // Genera y envía el código
    boolean initiateReset(String username, String phoneNumber);

    // Métodos auxiliares
    boolean hasPendingRequest(String username);
    
    int getWaitTime(String username);
    
    boolean resendCode(String username);
    
    VerifyResult verifyCode(String username, String code);
}