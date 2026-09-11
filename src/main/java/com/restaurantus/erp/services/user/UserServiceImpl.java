package com.restaurantus.erp.services.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.restaurantus.erp.dao.mysql.LoginAttemptDAO;
import com.restaurantus.erp.dao.mysql.RoleDAO;
import com.restaurantus.erp.dao.mysql.UserDAO;
import com.restaurantus.erp.domain.LoginAttempt;
import com.restaurantus.erp.domain.Role;
import com.restaurantus.erp.domain.User;
import com.restaurantus.erp.services.CustomUserDetailsService;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private UserDAO userRepository; // Usamos userRepository (estándar JPA)

    @Autowired
    private RoleDAO roleRepository;

    @Autowired
    private LoginAttemptDAO loginAttemptRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- MÉTODOS BÁSICOS DE LECTURA ---
    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public User findUser(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    @Override
    public User findUserByName(String name) {
        return this.userRepository.findByUsername(name).orElse(null);
    }

    @Override
    public List<User> findFiltered(String name, Long roleId, boolean showActive, boolean showInactive) {
        return this.userRepository.findByFilters(name, roleId, showActive, showInactive);
    }

    @Override
    public User findByUsernameVIEW(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // --- MÉTODOS DE ESCRITURA (GUARDAR/ACTUALIZAR) CON FOTOS ---

    @Override
    public void saveUser(User user, @Nullable MultipartFile file) throws Exception {
        // 1. Encriptar contraseña si es nuevo
        if (user.getId() == null || this.userRepository.findById(user.getId()).isEmpty()) {
            user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        }

        // 2. Gestión de Foto
        if (file != null && !file.isEmpty()) {
            String imageUrl = saveFileWithUniqueName(file);
            user.setFoto(imageUrl);
        } else {
            if (user.getFoto() == null || user.getFoto().isEmpty()) {
                user.setFoto("default-user.png");
            }
        }
        
        // 3. Guardar
        this.userRepository.save(user);
    }

    @Override
    public UserUpdateStatus updateUser(User user, @Nullable MultipartFile file) {
        UserUpdateStatus status = this.checkAdminErrors(user);

        if (!status.equals(UserUpdateStatus.SUCCESS)) {
            return status;
        }

        this.updateUserPhoto(user, file);

        if (!status.equals(UserUpdateStatus.SUCCESS)) {
            return status;
        }

        this.userRepository.save(user);

        // Si el usuario ha sido deshabilitado, invalidamos su sesión
        if (!user.isEnabled()) {
            this.expireUserSession(user.getUsername());
        } else {
            this.updateLoggedUserRoles(user);
        }

        return UserUpdateStatus.SUCCESS;
    }

    @Override
    public void deleteUser(Long id) {
        this.userRepository.findById(id).ifPresent((user) -> {
            if (!user.getUsername().equals("admin")) {
                // Borrar foto del disco
                if (user.getFoto() != null && !user.getFoto().contains("default-profile.png")) {
                    try {
                        Path rootPath = Paths.get("src/main/resources/static/images/profiles");
                        String nombreArchivo = Paths.get(user.getFoto()).getFileName().toString();
                        Files.deleteIfExists(rootPath.resolve(nombreArchivo));
                    } catch (IOException e) { }
                }
                this.userRepository.delete(user);
            }
        });
    }

    @Override
    public boolean updateUserPassword(Long userId, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) return false;
        User user = this.findUser(userId);
        if (user != null) {
            user.setPassword(this.passwordEncoder.encode(newPassword));
            this.userRepository.save(user);
            return true;
        }
        return false;
    }

    // --- MÉTODOS DE ROLES ---
    @Override
    public List<Role> getAllRoles() { return this.roleRepository.findAll(); }

    @Override
    public List<Role> findRoleFiltered(String name, boolean showActive, boolean showInactive) {
        return this.roleRepository.findByFilters(name, showActive, showInactive);
    }

    @Override
    public Role findRole(Long id) { return this.roleRepository.findById(id).orElse(null); }

    @Override
    public Role findRoleByName(String name) { return this.roleRepository.findByName(name).orElse(null); }

    @Override
    public void saveRole(Role role) { this.roleRepository.save(role); }

    // --- MÉTODOS PARA TELEGRAM Y ALERTAS ---

    @Override
    public User buscarPorToken(String token) {
        return this.userRepository.findByTelegramToken(token);
    }

    @Override
    public void guardarCambios(User user) {
        this.userRepository.save(user);
    }

    @Override
    public User buscarPorUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> buscarPorRoles(List<String> roles) {
        return userRepository.findByRoles_NameIn(roles);
    }

    @Override
    public List<User> buscarUsuariosParaAlertasStock() {
        // 1. Roles permitidos
        List<String> rolesAlmacen = Arrays.asList("ROLE_VISOR_ALMACEN", "ROLE_GESTOR_ALMACEN", "ROLE_ADMIN", "ROLE_GERENTE");
        
        // 2. Buscar usuarios con esos roles
        List<User> usuarios = userRepository.findByRoles_NameIn(rolesAlmacen);
        
        // 3. Filtrar solo los que tienen Telegram vinculado
        return usuarios.stream()
                .filter(u -> u.getTelegramChatId() != null && !u.getTelegramChatId().isEmpty())
                .collect(Collectors.toList());
    }

    // --- HELPERS ---
    @Override
    public List<LoginAttempt> getAllLoginAttempts() {
        return this.loginAttemptRepository.findAll();
    }

    private String saveFileWithUniqueName(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        
        String uniqueFilename;
        do {
            uniqueFilename = UUID.randomUUID().toString() + extension;
        } while (userRepository.existsByFoto(uniqueFilename));

        Path uploadPath = Paths.get("src/main/resources/static/images/profiles");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        
        Path path = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        
        return uniqueFilename;
    }

    //Comprueba si el admin tiene el rol de admin y si está habilitado
    private UserUpdateStatus checkAdminErrors(User user) {
        if (user.getUsername().equals("admin")) {
            boolean adminUserHasAdminRole = false;
            Object[] usrRoles = user.getRoles().toArray();

            for (int i = 0; i < user.getRoles().size(); ++i) {
                if (((Role) usrRoles[i]).getName().equals("ROLE_ADMIN")) {
                    adminUserHasAdminRole = true;
                }
            }

            if (!adminUserHasAdminRole) {
                return UserUpdateStatus.ERROR_ADMIN_ROLE_FROM_ADMIN;
            } else if (!user.isEnabled()) {
                return UserUpdateStatus.ERROR_DISABLE_ADMIN;
            }
        }

        return UserUpdateStatus.SUCCESS;
    }

    private UserUpdateStatus updateUserPhoto(User userForm, @Nullable MultipartFile file) {
        User userOriginal = this.userRepository.findById(userForm.getId()).orElse(null);

        if (file != null && !file.isEmpty()) {
            try {
                // Borrar foto vieja si no es la default
                if (userOriginal.getFoto() != null && !userOriginal.getFoto().isEmpty()
                        && !userOriginal.getFoto().contains("default-profile.png")) {
                        // 1. Obtenemos la ruta base del proyecto
                        Path rootPath = Paths.get("src/main/resources/static/images/profiles");
                        // 2. Obtenemos solo el nombre del archivo de la base de datos
                        String nombreArchivo = Paths.get(userOriginal.getFoto()).getFileName().toString();
                        // 3. Combinamos ambos
                        Path oldPath = rootPath.resolve(nombreArchivo);
                        Files.deleteIfExists(oldPath);
                }

                // Guardar la NUEVA foto
                String newImageUrl = this.saveFileWithUniqueName(file);
                userForm.setFoto(newImageUrl);
            } catch (IOException | InvalidPathException ex) {
                return UserUpdateStatus.ERROR_PHOTO;
            }
        }

        return UserUpdateStatus.SUCCESS;
    }

    private void updateLoggedUserRoles(User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        //Si el usuario que estamos editando es el mismo que está logueado, refrescamos roles
        if (auth != null && auth.getName().equals(user.getUsername())) {
            UserDetails newUserDetails = this.customUserDetailsService.loadUserByUsername(user.getUsername());

            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    newUserDetails,
                    auth.getCredentials(),
                    newUserDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }

    private void expireUserSession(String username) {
        List<Object> allPrincipals = this.sessionRegistry.getAllPrincipals();

        for (Object principal : allPrincipals) {
            if (principal instanceof UserDetails) {
                UserDetails loggedUser = (UserDetails) principal;

                // Si el nombre coincide con el usuario deshabilitado
                if (loggedUser.getUsername().equals(username)) {
                    // Obtenemos todas sus sesiones y las marcamos como expiradas
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

                    for (SessionInformation session : sessions) {
                        session.expireNow(); // <--- Esto lo envía al login en su siguiente clic
                    }
                }
            }
        }
    }
}