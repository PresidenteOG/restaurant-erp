package com.restaurantus.erp.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AESUtil {
    
    // CLAVE SECRETA (En producción esto iría en application.properties)
    // Debe tener 16 caracteres para AES-128
    @Value("${restaurantus.chat_secret_key}")
    private String secretKey; 

    public String encriptar(String data) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar: " + e.getMessage());
        }
    }

    public String desencriptar(String dataCifrada) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(this.secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dataCifrada)));
        } catch (Exception e) {
            return "[Texto ilegible]";
        }
    }
}