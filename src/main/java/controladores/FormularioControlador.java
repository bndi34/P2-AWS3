package controladores;

import Servicios.FormularioLocal;
import Servicios.UsuarioServices;
import io.javalin.http.Context;
import logica.Formulario;
import logica.Usuario;
import main.Main;
import util.Encriptacion;

import org.jetbrains.annotations.NotNull;
import Servicios.FormularioServices;

import java.util.*;
import java.util.stream.Collectors;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class FormularioControlador {

    public static void crearForm(@NotNull Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Creación de Formulario");
        modelo.put("accion", "/crud-Formulario/crear");

        ctx.render("/templates/crud-tradicional/crearEditarVisualizarFormulario.html", modelo);
    }

    public static void procesarCreacion(@NotNull Context ctx) throws Exception {

        System.out.println("asdfasdfa");
        long nuevoId = FormularioLocal.getInstancia().generarId();
                //FormularioServices.getInstancia().generarIdFormulario();
        String nombre = ctx.formParam("nombre");
        String nivelEscolar = ctx.formParam("nivelEscolar");
        String sector = ctx.formParam("sector");

        Usuario e;
        String username = ctx.sessionAttribute("USUARIO");

        if (username != null) {
            e = UsuarioServices.getInstancia().getUsuarioPorUsername(username);
        } else {
            e = UsuarioServices.getInstancia().getUsuarioPorUsername("default");
        }

        String latitudStr = ctx.formParam("latitud");
        String longitudStr = ctx.formParam("longitud");
        double latitud = 0.0;
        double longitud = 0.0;

        try {
            if (latitudStr != null && !latitudStr.isEmpty())
                latitud = Double.parseDouble(latitudStr);

            if (longitudStr != null && !longitudStr.isEmpty())
                longitud = Double.parseDouble(longitudStr);

        } catch (NumberFormatException ex) {
            throw new Exception("Los valores de latitud y longitud deben ser números válidos.");
        }

        Formulario form = new Formulario(nuevoId, nombre, sector, nivelEscolar, e, latitud, longitud,
                LocalDateTime.now(), false);

        //FormularioServices.getInstancia().crear(form);
        FormularioLocal.getInstancia().Crear(form);

        Main.enviarActualizacion("Formulario creado: " + form.getId());

        ctx.redirect("/crud-Formulario/");
    }

    public static void listar(@NotNull Context ctx) throws Exception {
        List<Formulario> lista = FormularioServices.getInstancia().findAll();
        List<Formulario> listalocal = FormularioLocal.getInstancia().listar();
        lista = lista.stream().filter(Objects::nonNull).collect(Collectors.toList());

        List<Formulario> listaCombinada = new ArrayList<>(lista);
        listaCombinada.addAll(listalocal);

        // Eliminar duplicados basados en el id del formulario
        Set<Formulario> setSinDuplicados = new LinkedHashSet<>(listaCombinada);
        List<Formulario> listaFinal = new ArrayList<>(setSinDuplicados);

        // Filtrar elementos nulos (si es necesario)
        listaFinal = listaFinal.stream().filter(Objects::nonNull).collect(Collectors.toList());

        // Filtrar elementos nulos (si es necesario)
        listaFinal = listaFinal.stream().filter(Objects::nonNull).collect(Collectors.toList());

        boolean sincronizar = listaFinal.stream()
                .anyMatch(formulario -> !formulario.getSincronizado());

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Listado de Formularios");
        modelo.put("lista", listaFinal);
        modelo.put("sincronizar", sincronizar); // Nueva variable

        // Leer Cookies y verificar si hay usuario logeado        
        if (ctx.cookie("COOKIEUSER") != null) 
            Encriptacion.getInstancia().setUserFromCookie(ctx);
        
        String usrString = ctx.sessionAttribute("USUARIO");
        Usuario usr = null;
        // Verificar si hay un usuario en la sesión
        if (usrString == null) {
            modelo.put("logState", "Login");
        } else 
        {
            modelo.put("logState", "Logout");
            usr = UsuarioServices.getInstancia().getUsuarioPorUsername(usrString);
        }


            if (usr != null && usr.getRol().equalsIgnoreCase("admin"))
            modelo.put("mostrarBoton", true);
         else 
            modelo.put("mostrarBoton", false); // Valor por defecto

        ctx.render("/templates/crud-tradicional/listarFormulario.html", modelo);
    }

    public static void eliminar(@NotNull Context ctx) throws Exception {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        //FormularioServices.getInstancia().eliminar(id);
        FormularioLocal.getInstancia().eliminando(id);

        Main.enviarActualizacion("Formulario eliminado: " + id);

        Thread.sleep(150);

        ctx.redirect("/crud-Formulario/");
    }

    public static void editarForm(@NotNull Context ctx) throws Exception {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Formulario formulario = FormularioLocal.getInstancia().getFormularioPorID(id);
        //FormularioServices.getInstancia().find(id);

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("UsuarioOriginal", formulario.getUsuario().getUsername());
        modelo.put("titulo", "Editar Formulario");
        modelo.put("accion", "/crud-Formulario/editar/" + id);
        modelo.put("formulario", formulario);

        ctx.render("/templates/crud-tradicional/crearEditarVisualizarFormulario.html", modelo);
    }

    public static void procesarEditar(@NotNull Context ctx) throws Exception {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Formulario formulario = FormularioLocal.getInstancia().getFormularioPorID(id);
                //FormularioServices.getInstancia().find(id);

        String username = ctx.formParam("UsuarioOriginal");


        Usuario e = UsuarioServices.getInstancia().getUsuarioPorUsername(username);

        String latitudStr = ctx.formParam("latitud");
        String longitudStr = ctx.formParam("longitud");
        double latitud = 0.0;
        double longitud = 0.0;
        try {
            if (latitudStr != null && !latitudStr.isEmpty())
                latitud = Double.parseDouble(latitudStr);

            if (longitudStr != null && !longitudStr.isEmpty())
                longitud = Double.parseDouble(longitudStr);

        } catch (NumberFormatException ex) {
            throw new Exception("Los valores de latitud y longitud deben ser números válidos.");
        }

        formulario.setNombre(ctx.formParam("nombre"));
        formulario.setSector(ctx.formParam("sector"));
        formulario.setNivelEscolar(ctx.formParam("nivelEscolar"));
        formulario.setUsuario(e);
        formulario.setLatitud(latitud);
        formulario.setLongitud(longitud);

        //FormularioServices.getInstancia().editar(formulario);
        FormularioLocal.getInstancia().actualizar(formulario);

        Main.enviarActualizacion("Formulario Editado: " + id);

        ctx.redirect("/crud-Formulario/");
    }

    public static void visualizar(@NotNull Context ctx) throws Exception {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        Formulario formulario = FormularioServices.getInstancia().find(id);

        if (formulario == null) {
            ctx.status(404).result("Artículo no encontrado");

            formulario = FormularioLocal.getInstancia().getFormularioPorID(id);

            if (formulario == null) {
                return;
            }
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Visualizar Artículo " + formulario.getId());
        modelo.put("nombreAutor", formulario.getUsuario().getNombre());
        modelo.put("formulario", formulario);
        modelo.put("visualizar", true); // Para controlar en el formulario si es visualizar
        modelo.put("accion", "/crud-Formulario/visualizar/" + formulario.getId() + "/crear");

        // Enviando al sistema de plantillas
        ctx.render("/templates/crud-tradicional/visualizarFormulario.html", modelo);
    }

    public static void agregarDB(@NotNull Context ctx) throws Exception {
        List<Formulario> listalocal = new ArrayList<>(FormularioLocal.getInstancia().listar());

        for (Formulario form : listalocal) {
            if (!form.getSincronizado()) {
                form.setSincronizado(true);
                FormularioServices.getInstancia().crear(form);
                FormularioLocal.getInstancia().eliminando(form.getId());
                Main.enviarActualizacion("Formulario Sincronizado: " + form.getId());
            }
        }

        ctx.redirect("/crud-Formulario/");
    }
}