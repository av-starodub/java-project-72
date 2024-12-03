package hexlet.code.utils;

public final class NamedRoutes {

    private NamedRoutes() {
    }

    public static String root() {
        return "/";
    }

    public static String urlNew() {
        return root() + "urls";
    }

    public static String urlsAll() {
        return root() + "urls";
    }

    public static String urlById() {
        return root() + "urls/{id}";
    }

    public static String urlById(Long id) {
        return root() + "urls/" + id;
    }

    public static String checkNew() {
        return root() + "urls/{id}/checks";
    }

    public static String checkNew(Long id) {
        return root() + "urls/" + id + "/checks";
    }

}
