package main;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import logica.*;
import org.jetbrains.annotations.NotNull;
import Servicios.UsuarioServices;

public class ApiControlador {
    public static void eliminarUsuario(@NotNull Context ctx) throws Exception {
        //parseando la informacion del POJO debe venir en formato json.
        ctx.json(UsuarioServices.getInstancia().eliminar(ctx.pathParamAsClass("username", String.class).get()));
    }

    public static void listarUsuarios(@NotNull Context ctx) throws Exception {
        ctx.json(UsuarioServices.getInstancia().findAll()); // Cambiado a listarUsuario
    }

    public static void usuarioPorUsername(@NotNull Context ctx) throws Exception {
        // Cambiado a getUsuarioPorUsername y se espera un String como username
        Usuario usuario = UsuarioServices.getInstancia().find(ctx.pathParam("username"));

        if (usuario != null) {
            ctx.json(usuario);
        } else {
            throw new NotFoundResponse("Usuario no encontrado");
        }
    }

    public static void crearUsuario(@NotNull Context ctx) throws Exception {
        // Parseando la información del POJO (debe venir en formato JSON)
        Usuario tmp = ctx.bodyAsClass(Usuario.class); // Cambiado a Usuario
        // Creando el usuario
        ctx.json(UsuarioServices.getInstancia().crear(tmp)); // Cambiado a crearUsuario
    }

    public static void actualizarUsuario(@NotNull Context ctx) throws Exception {
        // Obtener el username del usuario a actualizar desde la ruta
        String username = ctx.pathParam("username");

        // Parsear el cuerpo de la solicitud (en formato JSON) a un objeto Usuario
        Usuario usuarioActualizado = ctx.bodyAsClass(Usuario.class);

        // Verificar que el username en la ruta coincida con el username en el cuerpo de la solicitud
        if (!username.equals(usuarioActualizado.getUsername())) {
            return;
        }

        // Llamar al servicio para actualizar el usuario
        Usuario usuario = UsuarioServices.getInstancia().editar(usuarioActualizado);

        // Verificar si el usuario fue actualizado correctamente
        if (usuario != null) {
            // Retornar el usuario actualizado en formato JSON
            ctx.json(usuario);
        } else {
            // Si el usuario no existe, lanzar una excepción
            throw new NotFoundResponse("No se encontró el usuario con username: " + username);
        }
    }


}
