package cat.copernic.restaurantus_erp.domain;

public enum SubcategoriaPlato {
    // BEBIDAS
    GASEOSAS(CategoriaPlato.BEBIDA, "Gaseosas"),
    AGUAS_REFRESCANTES(CategoriaPlato.BEBIDA, "Aguas y Refrescantes"),
    CAFETERIA_INFUSIONES(CategoriaPlato.BEBIDA, "Cafetería e infusiones"),
    CERVEZAS(CategoriaPlato.BEBIDA, "Cervezas"),
    VINOS(CategoriaPlato.BEBIDA, "Vinos"),
    COCTELERIA(CategoriaPlato.BEBIDA, "Coctelería"),
    DESTILADOS_LICORES(CategoriaPlato.BEBIDA, "Destilados y licores"),

    // PLATOS
    ENTRANTES_FRIOS(CategoriaPlato.PLATO, "Entrantes fríos"),
    ENTRANTES_CALIENTES(CategoriaPlato.PLATO, "Entrantes calientes"),
    ENSALADAS_LIGEROS(CategoriaPlato.PLATO, "Ensaladas y platos ligeros"),
    PASTAS_ARROCES(CategoriaPlato.PLATO, "Pastas y arroces"),
    CARNES(CategoriaPlato.PLATO, "Carnes"),
    PESCADOS_MARISCOS(CategoriaPlato.PLATO, "Pescados y mariscos"),
    VEGETARIANO_VEGANO(CategoriaPlato.PLATO, "Opciones vegetarianas y veganas"),
    GUARNICIONES(CategoriaPlato.PLATO, "Guarniciones"),

    // POSTRES
    REPOSTERIA_TARTAS(CategoriaPlato.POSTRE, "Repostería y tartas"),
    POSTRES_LACTEOS(CategoriaPlato.POSTRE, "Postres lácteos y de cuchara"),
    FRUTA_NATURAL(CategoriaPlato.POSTRE, "Fruta natural"),
    HELADOS_SORBETES(CategoriaPlato.POSTRE, "Helados y sorbetes"),
    TABLA_QUESOS(CategoriaPlato.POSTRE, "Tabla de quesos");

    private final CategoriaPlato categoriaPadre;
    private final String nombreVisible;

    SubcategoriaPlato(CategoriaPlato categoriaPadre, String nombreVisible) {
        this.categoriaPadre = categoriaPadre;
        this.nombreVisible = nombreVisible;
    }

    public CategoriaPlato getCategoriaPadre() { return categoriaPadre; }
    public String getNombreVisible() { return nombreVisible; }
}
