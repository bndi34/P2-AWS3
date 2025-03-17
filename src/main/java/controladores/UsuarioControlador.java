package controladores;

import Servicios.FormularioServices;
import io.javalin.http.Context;
import logica.Formulario;
import logica.Usuario;
import org.jetbrains.annotations.NotNull;
import Servicios.UsuarioServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioControlador {
    public static void crearUsuarioForm(@NotNull Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Creación de Usuario");
        modelo.put("accion", "/crud-Usuario/crear");

        ctx.render("/templates/crud-tradicional/crearEditarVisualizarUsuario.html", modelo);
    }

    public static void procesarCreacionUsuario(@NotNull Context ctx) throws Exception {
        long nuevoId = UsuarioServices.getInstancia().generarIdUsuario();
        String username = ctx.formParam("username");
        String nombre = ctx.formParam("nombre");
        String password = ctx.formParam("password");
        String rol = ctx.formParam("rol");

        try {
            if (UsuarioServices.getInstancia().find(nuevoId) != null) {
                ctx.redirect("/crud-Usuario/");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json("Error al verificar la existencia del usuario");
            return;
        }

        Usuario tmp = new Usuario(nuevoId, username, nombre, password, rol);

        ctx.json(UsuarioServices.getInstancia().crear(tmp));
        ctx.redirect("/crud-Usuario/");
    }

    public static void listarUsuario(@NotNull Context ctx) throws Exception {
        List<Usuario> lista = UsuarioServices.getInstancia().findAll();

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Listado de Usuarios");
        modelo.put("lista", lista);

        ctx.render("/templates/crud-tradicional/listar.html", modelo);
    }

    public static void eliminarUsuario(@NotNull Context ctx) throws Exception {
        // Obtén el parámetro de ruta "username"
        String username = ctx.pathParam("username");

        // Busca el usuario por su username
        Usuario usuario = UsuarioServices.getInstancia().getUsuarioPorUsername(username);

        // Si el usuario existe, elimínalo
        if (usuario != null) {
            UsuarioServices.getInstancia().eliminar(usuario.getId()); // Elimina por ID
        }

        // Redirige a la lista de usuarios
        ctx.redirect("/crud-Usuario/");
    }

    public static void editarUsuarioForm(@NotNull Context ctx) throws Exception {
        Usuario usuario = UsuarioServices.getInstancia().getUsuarioPorUsername(ctx.pathParam("username"));

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Editar Usuario " + usuario.getUsername());
        modelo.put("usuario", usuario);
        modelo.put("accion", "/crud-Usuario/editar");

        ctx.render("/templates/crud-tradicional/crearEditarVisualizarUsuario.html", modelo);
    }

    public static void procesarEditarUsuario(@NotNull Context ctx) throws Exception {
        long idArticulo = Long.parseLong(ctx.formParam("id"));
        String username = ctx.formParam("username");
        String nombre = ctx.formParam("nombre");
        String password = ctx.formParam("password");
        String rol = ctx.formParam("rol");

        Usuario tmp = new Usuario(idArticulo, username, nombre, password, rol);

        UsuarioServices.getInstancia().editar(tmp);
        ctx.redirect("/crud-Usuario/");
    }
}