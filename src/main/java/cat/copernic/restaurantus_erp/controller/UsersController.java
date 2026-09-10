package cat.copernic.restaurantus_erp.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cat.copernic.restaurantus_erp.domain.Role;
import cat.copernic.restaurantus_erp.domain.User;
import cat.copernic.restaurantus_erp.services.user.UserService;
import cat.copernic.restaurantus_erp.services.user.UserUpdateStatus;

@Controller
public class UsersController {
    @Autowired
    private UserService userService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/users/")
    public String listUsers(@RequestParam(required = false) String username,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) boolean showActive,
            @RequestParam(required = false) boolean showInactive,
            Model model,
            Authentication authentication) {

        // Si ambos son false se muestran los dos
        if (!showActive && !showInactive) {
            showActive = true;
            showInactive = true;
        }

        model.addAttribute("users", this.userService.findFiltered(username, roleId, showActive, showInactive));
        model.addAttribute("roles", this.userService.findRoleFiltered("", true, false));
        model.addAttribute("activeChecked", showActive);
        model.addAttribute("inactiveChecked", showInactive);
        model.addAttribute("activePage", "rolesAndUsers");
        return "users/usersList";
    }

    @GetMapping("/users/register-new")
    public String createNewUser(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        List<Role> roles = this.userService.getAllRoles();
        model.addAttribute("allRoles", roles);
        return "users/altaUsuario";
    }

    @PostMapping("/users/register-new")
    public String submitUserCreate(User user, @RequestParam("fotoFile") MultipartFile file, Model model, Locale locale) throws Exception {
        if (this.userService.findUserByName(user.getUsername()) != null) {
            model.addAttribute("error", messageSource.getMessage("usuarios.alta.error_username_already_in_use", null, locale));
            return "users/altaUsuario";
        }

        this.userService.saveUser(user, file);
        return "redirect:/users/";
    }

    @GetMapping("/users/view-{id}")
    public String openViewDetail(@PathVariable Long id, Model model) {
        User user = this.userService.findUser(id);
        model.addAttribute("user", user);
        return "users/userDetail";
    }

    @GetMapping("/users/edit-{id}")
    public String openUserEdit(@PathVariable Long id, Model model) {
        List<Role> roles = this.userService.getAllRoles();
        model.addAttribute("allRoles", roles);
        User user = this.userService.findUser(id);
        model.addAttribute("user", user);
        return "users/userUpdate";
    }

    @PostMapping("/users/updateUser")
    public String submitUserUpdate(User user, MultipartFile fotoFile, Model model, Locale locale) throws Exception {
        UserUpdateStatus status = this.userService.updateUser(user, fotoFile);

        if (!status.equals(UserUpdateStatus.SUCCESS)) {
            String errorMessage;

            if (status.equals(UserUpdateStatus.ERROR_ADMIN_ROLE_FROM_ADMIN)) {
                errorMessage = messageSource.getMessage("userUpdate.error.admin_role", null, locale);
            } else if (status.equals(UserUpdateStatus.ERROR_DISABLE_ADMIN)) {
                errorMessage = messageSource.getMessage("userUpdate.error.admin_disable", null, locale);
            } else {
                errorMessage = messageSource.getMessage("userUpdate.error.photo", null, locale);
            }

            model.addAttribute("allRoles", this.userService.getAllRoles());
            model.addAttribute("user", user);
            model.addAttribute("error", errorMessage);
            return "users/userUpdate";
        }

        return "redirect:/users/";
    }

    @GetMapping("/users/admin-password-{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showChangePasswordForm(@PathVariable Long id, Model model) {
        User user = this.userService.findUser(id);
        model.addAttribute("user", user);
        return "users/adminPassword";
    }

    @PostMapping("/users/adminUpdatePassword")
    @PreAuthorize("hasRole('ADMIN')")
    public String updatePassword(@RequestParam("userId") Long userId,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (!this.userService.updateUserPassword(userId, newPassword, confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les contrasenyes no coincideixen.");
            return "redirect:/users/admin-password-" + userId;
        } else {
            redirectAttributes.addFlashAttribute("success", "Contrasenya actualitzada correctament.");
            return "redirect:/users/";
        }
    }

    @GetMapping("/users/delete-{id}")
    public String askDeleteConfirmation(@PathVariable Long id, Model model) {
        User user = this.userService.findUser(id);
        model.addAttribute("user", user);
        return "users/userDelete";
    }

    @GetMapping("/users/deleteUserOk-{id}")
    public String confirmedDelete(@PathVariable Long id, Model model) {
        this.userService.deleteUser(id);
        return "redirect:/users/";
    }

    @GetMapping("/roles")
    public String listRoles(@RequestParam(required = false) String rolename,
            @RequestParam(required = false) boolean showActive,
            @RequestParam(required = false) boolean showInactive,
            Model model) {

        // Si ambos son false se mostrará al menos los activos
        if (!showActive && !showInactive) {
            showActive = true;
        }

        model.addAttribute("roles", this.userService.findRoleFiltered(rolename, showActive, showInactive));
        model.addAttribute("activeChecked", showActive);
        model.addAttribute("inactiveChecked", showInactive);
        model.addAttribute("activePage", "rolesAndUsers");
        return "roles/rolesList";
    }

    @GetMapping("/roles/createRole")
    public String createNewRole(Model model) {
        Role role = new Role();
        model.addAttribute("role", role);
        return "roles/roleName";
    }

    @GetMapping("/roles/rename-{id}")
    public String openRoleRename(@PathVariable Long id, Model model) {
        Role role = this.userService.findRole(id);
        model.addAttribute("role", role);
        return "roles/roleName";
    }

    @PostMapping("/roles/updateRole")
    public String submitRoleName(Role role, Model model, Locale locale) {
        if (this.userService.findRoleByName(role.getName()) != null) {
            String errorMessage = messageSource.getMessage("roleName.error.duplicate", null, locale);
            model.addAttribute("error", errorMessage);
            return "roles/roleName";
        }

        Role role1 = this.userService.findRole(role.getId());
        role.setActive(role1.isActive());//per evitar un bug en el que es reseteja l'active
        this.userService.saveRole(role);
        return "redirect:/roles";
    }

    @GetMapping("/roles/disable-{id}")
    public String askDisableConfirmation(@PathVariable Long id, Model model) {
        Role role = this.userService.findRole(id);
        model.addAttribute("role", role);
        return "roles/roleDisable";
    }

    @GetMapping("/roles/disableRoleOk-{id}")
    public String confirmedDisable(@PathVariable Long id, Model model) {
        Role role = this.userService.findRole(id);
        role.setActive(false);
        this.userService.saveRole(role);
        return "redirect:/roles";
    }

    @GetMapping("/roles/enableRole-{id}")
    public String enableRole(@PathVariable Long id, Model model) {
        Role role = this.userService.findRole(id);
        role.setActive(true);
        this.userService.saveRole(role);
        return "redirect:/roles";
    }

    @PostMapping("/guardar-usuario")
    public String guardarUsuario(User user) { // Cambiado 'User User' por 'User user' (convención)

        // 1. Generamos el token para Telegram
        String token = java.util.UUID.randomUUID().toString();
        user.setTelegramToken(token);

        // 2. Guardamos el usuario (El saveUser ya hashea la contraseña cuando el usuario es nuevo)
        // NOTA: Usa 'this.userService', no 'UserService' (que es la clase)
        //this.userService.saveUser(user);
        // 3. Redirigimos pasando el token para generar el enlace
        return "redirect:/registro-exito?token=" + token;
    }

    // Necesitas una vista simple para mostrar el enlace
    @GetMapping("/registro-exito")
    public String registroExito(Model model, String token) {
        model.addAttribute("token", token);
        // Nombre de tu Bot de Telegram (cámbialo por el real)
        model.addAttribute("botName", "TuBotDeRestaurantus");
        return "registroExito"; // Tienes que crear este HTML
    }

    @GetMapping("/loginAttempts")
    public String listLoginAttempts(Model model) {
        model.addAttribute("attempts", this.userService.getAllLoginAttempts());
        model.addAttribute("activePage", "rolesAndUsers");
        return "loginAttempts";
    }

    @GetMapping("/error/403")
    public String accessDenied() {
        return "error/403"; // Busca en templates/error/403.html
    }

    @GetMapping("/comenzar-trabajo")
    public String redirigirSegunRol(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        var roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Prioridad según tu configuración de SecurityConfig
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_GERENTE")) {
            return "redirect:/users/"; // O la ruta de gestión de usuarios
        }

        if (roles.contains("ROLE_JEFE_COCINA") || roles.contains("ROLE_COCINERO")) {
            return "redirect:/cocina/pendientes";
        }

        if (roles.contains("ROLE_CAMARERO")) {
            return "redirect:/pedidos/listado";
        }

        if (roles.contains("ROLE_CAJA") || roles.contains("ROLE_FACTURISTA")) {
            return "redirect:/finanzas/pendientesCobro";
        }

        if (roles.contains("ROLE_GESTOR_ALMACEN") || roles.contains("ROLE_VISOR_ALMACEN")) {
            return "redirect:/almacen/inventario";
        }

        if (roles.contains("ROLE_GESTOR_CLIENTES") || roles.contains("ROLE_VISOR_CLIENTES")) {
            return "redirect:/reservas/listar";
        }

        if (roles.contains("ROLE_GESTOR_CARTA") || roles.contains("ROLE_VISOR_CARTA")) {
            return "redirect:/carta";
        }

        return "redirect:/home"; // Por defecto
    }
}
