package cat.copernic.restaurantus_erp.services.resetpassword;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cat.copernic.restaurantus_erp.dao.mysql.UserDAO;
import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.telegram.TelegramService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // <--- ESTO genera el constructor automáticamente para los campos 'final'
public class SecurityResetServiceImpl implements SecurityResetService {

    // Al ser 'final', Lombok sabe que debe inyectarlos en el constructor.
    // Ya no hace falta @Autowired aquí.
    private final TelegramService telegramService;
    private final UserDAO userDAO;

    // Esto no se inyecta, se inicializa aquí
    private final Map<String, OtpInfo> otpStorage = new ConcurrentHashMap<>();

    @Override
    public String validateUserForReset(String username, String phoneNumber) {
        User user = userDAO.findByUsername(username).orElse(null);

        boolean usuarioInvalido = (user == null || !user.isEnabled());
        boolean telefonoMal = (user != null && !phoneNumber.equals(user.getPhoneNumber()));
        boolean sinTelegram = (user != null && (user.getTelegramChatId() == null || user.getTelegramChatId().trim().isEmpty()));

        if (usuarioInvalido || telefonoMal || sinTelegram) {
            return "forgot_pwd.error.validation_failed";
        }
        return null;
    }

    @Override
    public boolean initiateReset(String username, String phoneNumber) {
        User user = userDAO.findByUsername(username).orElse(null);
        if (user == null) return false;
        
        generateAndSendCode(user, 0);
        return true;
    }

    private void generateAndSendCode(User user, int currentResends) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        // Usamos el constructor generado por Lombok
        OtpInfo info = new OtpInfo(code, 0, currentResends, System.currentTimeMillis());
        
        otpStorage.put(user.getUsername(), info);
        telegramService.enviarMensaje(user.getTelegramChatId(), "🔐 Tu código de recuperación: " + code);
    }

    @Override
    public boolean resendCode(String username) {
        if (!otpStorage.containsKey(username)) return false;
        OtpInfo oldInfo = otpStorage.get(username);
        User user = userDAO.findByUsername(username).orElse(null);
        if (user != null) {
            generateAndSendCode(user, oldInfo.getResendCount() + 1); // Lombok generó el getter
            return true;
        }
        return false;
    }

    @Override
    public VerifyResult verifyCode(String username, String codeInput) {
        if (!otpStorage.containsKey(username)) return VerifyResult.EXPIRED;
        OtpInfo info = otpStorage.get(username);
        User user = userDAO.findByUsername(username).orElse(null);
        if (user == null) return VerifyResult.ERROR;

        if (info.getCode().equals(codeInput)) { // Getter automático
            otpStorage.remove(username);
            return VerifyResult.SUCCESS;
        } else {
            info.setAttempts(info.getAttempts() + 1); // Setter y Getter automáticos
            if (info.getAttempts() >= 3) {
                user.setEnabled(false);
                userDAO.save(user);
                otpStorage.remove(username);
                telegramService.enviarMensaje(user.getTelegramChatId(), "🚫 ALERTA: Cuenta bloqueada por seguridad.");
                return VerifyResult.LOCKED;
            }
            return VerifyResult.INVALID;
        }
    }

    @Override
    public boolean hasPendingRequest(String username) {
        return otpStorage.containsKey(username);
    }

    @Override
    public int getWaitTime(String username) {
        if (!otpStorage.containsKey(username)) return 30;
        return otpStorage.get(username).getResendCount() == 0 ? 30 : 60;
    }

    public enum VerifyResult { SUCCESS, INVALID, LOCKED, EXPIRED, ERROR }

    // --- CLASE INTERNA LIMPIA CON LOMBOK ---
    @Data // Genera Getters, Setters, toString, etc.
    @AllArgsConstructor // Genera constructor con todos los argumentos
    private static class OtpInfo {
        private String code;
        private int attempts;
        private int resendCount;
        private long timestamp;
    }
}