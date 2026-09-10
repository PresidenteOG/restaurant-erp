package cat.copernic.restaurantus_erp.services.user;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.restaurantus_erp.domain.LoginAttempt;
import cat.copernic.restaurantus_erp.domain.Role;
import cat.copernic.restaurantus_erp.domain.User;


public interface UserService {
    public List<User> getAllUsers();

    public List<User> findFiltered(String name, Long roleId, boolean showActive, boolean showInactive);

    public User findUser(Long id);

    public User findUserByName(String name);

    public void saveUser(User user, @Nullable MultipartFile file) throws Exception;

    public UserUpdateStatus updateUser(User user, @Nullable MultipartFile file);

    public void deleteUser(Long id);

    public boolean updateUserPassword(Long userId, String newPassword, String confirmPassword);

    public User buscarPorToken(String token);

    public void guardarCambios(User user);// Para actualizaciones simples (Bot)

    public List<Role> getAllRoles();

    public List<Role> findRoleFiltered(String name, boolean showActive, boolean showInactive);

    public Role findRole(Long id);

    public Role findRoleByName(String name);

    public void saveRole(Role role);

    public User buscarPorUsername(String username);

    public List<LoginAttempt> getAllLoginAttempts();

    User findByUsernameVIEW(String username); 

    List<User> buscarPorRoles(List<String> roles);

    List<User> buscarUsuariosParaAlertasStock();
}
