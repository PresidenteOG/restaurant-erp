package cat.copernic.restaurantus_erp.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cat.copernic.restaurantus_erp.dao.mysql.ArqueoCajaDAO;
import cat.copernic.restaurantus_erp.dao.mysql.MenuDAO;
import cat.copernic.restaurantus_erp.dao.mysql.MesaDAO;
import cat.copernic.restaurantus_erp.dao.mysql.PedidoDAO;
import cat.copernic.restaurantus_erp.dao.mysql.PlatoDAO;
import cat.copernic.restaurantus_erp.dao.mysql.RoleDAO;
import cat.copernic.restaurantus_erp.dao.mysql.UbicacionMesaDAO;
import cat.copernic.restaurantus_erp.dao.mysql.UserDAO;
import cat.copernic.restaurantus_erp.domain.ArqueoCaja;
import cat.copernic.restaurantus_erp.domain.CategoriaPlato;
import cat.copernic.restaurantus_erp.domain.EstadoPedido;
import cat.copernic.restaurantus_erp.domain.Menu;
import cat.copernic.restaurantus_erp.domain.Mesa;
import cat.copernic.restaurantus_erp.domain.MetodoPago;
import cat.copernic.restaurantus_erp.domain.Pedido;
import cat.copernic.restaurantus_erp.domain.PedidoPlato;
import cat.copernic.restaurantus_erp.domain.Plato;
import cat.copernic.restaurantus_erp.domain.Role;
import cat.copernic.restaurantus_erp.domain.SubcategoriaPlato;
import cat.copernic.restaurantus_erp.domain.UbicacionMesa;
import cat.copernic.restaurantus_erp.domain.User;

/**
 * Rellena la base H2 del perfil demo con un restaurante de ejemplo:
 * roles, personal, zonas y mesas, una carta pequena, un par de menus,
 * un turno de caja abierto y algunos pedidos en curso.
 *
 * Todas las cuentas usan la contrasena "demo1234".
 * Cuenta principal: admin / demo1234 (ROLE_ADMIN).
 */
@Component
@Profile("demo")
@Order(1)
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "demo1234";

    private final RoleDAO roleDAO;
    private final UserDAO userDAO;
    private final UbicacionMesaDAO ubicacionDAO;
    private final MesaDAO mesaDAO;
    private final PlatoDAO platoDAO;
    private final MenuDAO menuDAO;
    private final PedidoDAO pedidoDAO;
    private final ArqueoCajaDAO arqueoDAO;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(RoleDAO roleDAO, UserDAO userDAO, UbicacionMesaDAO ubicacionDAO,
                          MesaDAO mesaDAO, PlatoDAO platoDAO, MenuDAO menuDAO, PedidoDAO pedidoDAO,
                          ArqueoCajaDAO arqueoDAO, PasswordEncoder passwordEncoder) {
        this.roleDAO = roleDAO;
        this.userDAO = userDAO;
        this.ubicacionDAO = ubicacionDAO;
        this.mesaDAO = mesaDAO;
        this.platoDAO = platoDAO;
        this.menuDAO = menuDAO;
        this.pedidoDAO = pedidoDAO;
        this.arqueoDAO = arqueoDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userDAO.count() > 0) {
            return; // ya sembrado
        }
        System.out.println("🌱 Perfil demo: sembrando datos de ejemplo…");

        Map<String, Role> roles = seedRoles();
        seedUsers(roles);
        List<UbicacionMesa> zonas = seedZonas();
        List<Mesa> mesas = seedMesas(zonas);
        List<Plato> platos = seedPlatos();
        seedMenus(platos);
        seedArqueoAbierto();
        seedPedidos(mesas, platos);

        System.out.println("✅ Demo lista. Entra con  admin / " + DEMO_PASSWORD);
    }

    private Map<String, Role> seedRoles() {
        String[] nombres = {
            "ROLE_ADMIN", "ROLE_GERENTE", "ROLE_CAMARERO", "ROLE_JEFE_COCINA", "ROLE_COCINERO",
            "ROLE_CAJA", "ROLE_GESTOR_CARTA", "ROLE_VISOR_CARTA", "ROLE_GESTOR_ALMACEN",
            "ROLE_VISOR_ALMACEN", "ROLE_GESTOR_CLIENTES", "ROLE_VISOR_CLIENTES"
        };
        Map<String, Role> map = new LinkedHashMap<>();
        for (String n : nombres) {
            Role r = new Role();
            r.setName(n);
            r.setActive(true);
            r.setInternal(!n.equals("ROLE_GESTOR_CLIENTES") && !n.equals("ROLE_VISOR_CLIENTES"));
            map.put(n, roleDAO.save(r));
        }
        return map;
    }

    private void seedUsers(Map<String, Role> roles) {
        createUser("admin", "admin@restaurantus.demo", roles, "ROLE_ADMIN");
        createUser("marta.gerente", "marta@restaurantus.demo", roles, "ROLE_GERENTE");
        createUser("pau.cambrer", "pau@restaurantus.demo", roles, "ROLE_CAMARERO", "ROLE_CAJA");
        createUser("nadia.cuina", "nadia@restaurantus.demo", roles, "ROLE_JEFE_COCINA", "ROLE_COCINERO");
        createUser("jordi.barra", "jordi@restaurantus.demo", roles, "ROLE_CAJA", "ROLE_VISOR_CARTA");
    }

    private void createUser(String username, String email, Map<String, Role> roles, String... roleNames) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        u.setEnabled(true);
        u.setFailedAttempts(0);
        u.setFoto("default-profile.png");
        // El perfil demo apaga Telegram, pero dejamos un valor no vacio para que
        // ni el interceptor ni el success handler redirijan a /telegram/vincular.
        u.setTelegramChatId("demo");
        Set<Role> set = new HashSet<>();
        for (String rn : roleNames) {
            set.add(roles.get(rn));
        }
        u.setRoles(set);
        userDAO.save(u);
    }

    private List<UbicacionMesa> seedZonas() {
        return ubicacionDAO.saveAll(List.of(
            new UbicacionMesa("Sala principal"),
            new UbicacionMesa("Terrassa"),
            new UbicacionMesa("Sala VIP")
        ));
    }

    private List<Mesa> seedMesas(List<UbicacionMesa> zonas) {
        List<Mesa> mesas = new ArrayList<>();
        int[] capacidades = {2, 2, 4, 4, 6, 4, 2, 8};
        for (int i = 0; i < capacidades.length; i++) {
            Mesa m = new Mesa();
            m.setNumMesa(String.valueOf(i + 1));
            m.setCapacidad(capacidades[i]);
            m.setUbicacion(zonas.get(i % zonas.size()).getNombre());
            m.setEstado(i < 3 ? "unavailable" : "available");
            mesas.add(m);
        }
        return mesaDAO.saveAll(mesas);
    }

    private List<Plato> seedPlatos() {
        List<Plato> platos = new ArrayList<>();
        platos.add(plato("Pa amb tomàquet", 3.50, CategoriaPlato.PLATO, SubcategoriaPlato.ENTRANTES_FRIOS, 5));
        platos.add(plato("Croquetes de pernil (6 u.)", 7.90, CategoriaPlato.PLATO, SubcategoriaPlato.ENTRANTES_CALIENTES, 12));
        platos.add(plato("Amanida de burrata", 9.50, CategoriaPlato.PLATO, SubcategoriaPlato.ENSALADAS_LIGEROS, 8));
        platos.add(plato("Arròs de carxofa i gambes", 16.00, CategoriaPlato.PLATO, SubcategoriaPlato.PASTAS_ARROCES, 25));
        platos.add(plato("Entrecot a la brasa", 21.50, CategoriaPlato.PLATO, SubcategoriaPlato.CARNES, 18));
        platos.add(plato("Lluç al forn amb verduretes", 18.00, CategoriaPlato.PLATO, SubcategoriaPlato.PESCADOS_MARISCOS, 20));
        platos.add(plato("Risotto de bolets", 14.50, CategoriaPlato.PLATO, SubcategoriaPlato.VEGETARIANO_VEGANO, 16));
        platos.add(plato("Coulant de xocolata", 6.50, CategoriaPlato.POSTRE, SubcategoriaPlato.REPOSTERIA_TARTAS, 10));
        platos.add(plato("Crema catalana", 5.00, CategoriaPlato.POSTRE, SubcategoriaPlato.POSTRES_LACTEOS, 5));
        platos.add(plato("Aigua mineral 50 cl", 2.20, CategoriaPlato.BEBIDA, SubcategoriaPlato.AGUAS_REFRESCANTES, 1));
        platos.add(plato("Copa de vi negre D.O. Priorat", 4.80, CategoriaPlato.BEBIDA, SubcategoriaPlato.VINOS, 2));
        platos.add(plato("Cervesa artesana", 3.90, CategoriaPlato.BEBIDA, SubcategoriaPlato.CERVEZAS, 2));
        return platoDAO.saveAll(platos);
    }

    private Plato plato(String nombre, double precio, CategoriaPlato cat, SubcategoriaPlato sub, int minutos) {
        Plato p = new Plato();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setCategoria(cat);
        p.setSubcategoria(sub);
        p.setEspecial(false);
        p.setImgUrl("default.png");
        p.setTiempoPreparacion(minutos);
        return p;
    }

    private void seedMenus(List<Plato> platos) {
        Menu migdia = new Menu();
        migdia.setNombre("Menú del migdia");
        migdia.setPrecio(15.90);
        migdia.setPlatos(List.of(platos.get(0), platos.get(2), platos.get(3), platos.get(8), platos.get(9)));

        Menu degustacio = new Menu();
        degustacio.setNombre("Menú degustació");
        degustacio.setPrecio(34.00);
        degustacio.setPlatos(List.of(platos.get(1), platos.get(2), platos.get(4), platos.get(5), platos.get(7), platos.get(10)));

        menuDAO.saveAll(List.of(migdia, degustacio));
    }

    private void seedArqueoAbierto() {
        ArqueoCaja a = new ArqueoCaja();
        a.setFechaApertura(LocalDateTime.now().minusHours(3));
        a.setEfectivoInicial(150.0);
        a.setVentasEfectivo(0.0);
        a.setVentasTarjeta(0.0);
        a.setSalidasEfectivo(0.0);
        a.setCajaAbierta(1);
        arqueoDAO.save(a);
    }

    private void seedPedidos(List<Mesa> mesas, List<Plato> platos) {
        pedido(mesas.get(0), EstadoPedido.PENDIENTE, "Sense gluten a la taula", List.of(
            linea(platos.get(1), 1), linea(platos.get(4), 2), linea(platos.get(10), 2)));
        pedido(mesas.get(1), EstadoPedido.PREPARANDO, null, List.of(
            linea(platos.get(3), 2), linea(platos.get(6), 1), linea(platos.get(9), 3)));
        pedido(mesas.get(2), EstadoPedido.LISTO, "Postres per emportar", List.of(
            linea(platos.get(7), 2), linea(platos.get(8), 1)));
    }

    private PedidoPlato linea(Plato plato, int cantidad) {
        PedidoPlato pp = new PedidoPlato();
        pp.setPlato(plato);
        pp.setCantidad(cantidad);
        pp.setEstado("PENDIENTE");
        pp.setEsMenu(false);
        return pp;
    }

    private void pedido(Mesa mesa, EstadoPedido estado, String observaciones, List<PedidoPlato> lineas) {
        Pedido p = new Pedido();
        p.setMesa(mesa);
        p.setEstado(estado);
        p.setObservaciones(observaciones);
        p.setFechaEntrada(LocalDateTime.now().minusMinutes(20));
        for (PedidoPlato l : lineas) {
            l.setPedido(p);
        }
        p.setDetallesPlatos(new ArrayList<>(lineas));
        p.calcularTotal();
        pedidoDAO.save(p);
    }
}
