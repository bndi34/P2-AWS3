package main;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import Servicios.UsuarioServices;
import controladores.FormularioControlador;
import controladores.UsuarioControlador;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.http.Cookie;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.RouteOverviewPlugin;
import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.websocket.WsContext;
import logica.Usuario;

import org.eclipse.jetty.server.ServerConnector;
import org.jasypt.util.text.BasicTextEncryptor;
import util.Encriptacion;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Main {

    private static int puerto = 7086;
    private static String lanIp = "192.168.1.7"; // Cambia esto por tu IP de red LAN

    private static List<WsContext> clientesConectados = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        inicializarDatos();
        
        // Creando la instancia del servidor y configurando.
        Javalin app = Javalin.create(config -> {
            // Configurar el servidor para escuchar en la IP de la red LAN
            /* 
            config.jetty.modifyServer(server -> {
                // Crear un nuevo conector para escuchar en la IP y puerto especificados
                ServerConnector connector = new ServerConnector(server);
                connector.setHost(lanIp); // Escuchar en la IP de la red LAN
                connector.setPort(puerto); // Escuchar en el puerto especificado
                server.addConnector(connector); // Agregar el conector al servidor
            });*/

            // Configurar los documentos estáticos.
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/";
                staticFileConfig.directory = "/publico";
                staticFileConfig.location = Location.CLASSPATH;
                staticFileConfig.precompress = false;
                staticFileConfig.aliasCheck = null;
            });

            // Configurar el sistema de plantilla por defecto.
            config.fileRenderer(new JavalinThymeleaf());

            // Configurar las rutas
            config.router.apiBuilder(() -> {
                path("/api", () -> {
                    path("/usuario", () -> {
                        ApiBuilder.get(ApiControlador::listarUsuarios);
                        ApiBuilder.post(ApiControlador::crearUsuario);
                        ApiBuilder.put(ApiControlador::actualizarUsuario);
                        path("/{username}", () -> {
                            ApiBuilder.get(ApiControlador::usuarioPorUsername);
                            ApiBuilder.delete(ApiControlador::eliminarUsuario);
                        });
                    });
                });

                path("/crud-Usuario/", () -> {
                    get(ctx -> {
                        ctx.redirect("/crud-Usuario/listar");
                    });
                    get("/listar", UsuarioControlador::listarUsuario);
                    get("/crear", UsuarioControlador::crearUsuarioForm);
                    post("/crear", UsuarioControlador::procesarCreacionUsuario);
                    get("/editar/{username}", UsuarioControlador::editarUsuarioForm);
                    post("/editar", UsuarioControlador::procesarEditarUsuario);
                    get("/eliminar/{username}", UsuarioControlador::eliminarUsuario);
                });

                path("/crud-Formulario/", () -> {
                    get(ctx -> {
                        ctx.redirect("/crud-Formulario/listar");
                    });
                    get("/listar", FormularioControlador::listar);
                    get("/crear", FormularioControlador::crearForm);
                    post("/crear", FormularioControlador::procesarCreacion);
                    get("/editar/{id}", FormularioControlador::editarForm);
                    post("/editar/{id}", FormularioControlador::procesarEditar);
                    get("/eliminar/{id}", FormularioControlador::eliminar);
                    get("/sincronizar", FormularioControlador::agregarDB);
                    get("/visualizar/{id}", FormularioControlador::visualizar);
                });
            });

            config.registerPlugin(new RouteOverviewPlugin());
        });

        app.ws("/ws/formularios", ws -> {
            ws.onConnect(ctx -> {
                clientesConectados.add(ctx);
                System.out.println("Cliente conectado: " + ctx.sessionId());
            });
            ws.onClose(ctx -> {
                clientesConectados.remove(ctx);
                System.out.println("Cliente desconectado: " + ctx.sessionId());
            });
            ws.onMessage(ctx -> {
                // Puedes manejar mensajes entrantes si es necesario
                System.out.println("Mensaje recibido: " + ctx.message());
            });
        });

        app.get("/resetCookie", ctx -> {
            ctx.removeCookie("COOKIEUSER");
            ctx.result("Cookie eliminada");
            ctx.redirect("/");
        });

        app.get("/login", ctx -> {
            if (ctx.sessionAttribute("USUARIO") != null) {
                ctx.sessionAttribute("USUARIO", null);
                ctx.removeCookie("COOKIEUSER");
                ctx.redirect("/");
            } else {
                ctx.redirect("/login.html");
            }
        });

        app.get("/", ctx -> ctx.redirect("/crud-Formulario/listar"));

        app.post("/loginAttempt", ctx -> {
            // Obtener el usuario y la contraseña
            String username = ctx.formParam("usuario");
            String password = ctx.formParam("password");
            System.out.println("Usuario: " + username + " Password: " + password);

            // Buscar el usuario en FakeServices
            Usuario user = UsuarioServices.getInstancia().getUsuarioPorUsername(username);
            System.out.println(user);

            // Ver si el usuario existe y si contraseña correcta
            if (user != null && user.getPassword().equals(password)) {
                // Guardar el usuario en la sesión
                ctx.sessionAttribute("USUARIO", user.getUsername());

                // Crear Cookie si el checkbox de recordar usuario está marcado
                if (ctx.formParam("rememberMe") != null) {
                    // Encriptar
                    Encriptacion encriptacion = Encriptacion.getInstancia();
                    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
                    textEncryptor.setPasswordCharArray(encriptacion.passwordEncriptacion().toCharArray());
                    String encryptedUser = textEncryptor.encrypt(username);

                    Cookie cookie = new Cookie("COOKIEUSER", encryptedUser);
                    cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(7));
                    ctx.cookie(cookie);
                }

                ctx.redirect("/");
            } else {
                ctx.status(401).result("Usuario o pass incorrecta");
            }
        });

        app.start(puerto);
    }

    public static void inicializarDatos() {
        if (UsuarioServices.getInstancia().findAll().isEmpty()) {
            UsuarioServices serviceUsuario = new UsuarioServices();

            Usuario useri = new Usuario(10L, "admin", "admin", "admin", "admin");
            serviceUsuario.crear(useri);

            serviceUsuario.crear(new Usuario(9L, "default", "", "", ""));
        }
    }

    public static void enviarActualizacion(String mensaje) {
        for (WsContext cliente : clientesConectados) {
            cliente.send(mensaje);
        }
    }
}