package util;

import io.javalin.http.Context;
import logica.Usuario;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jetbrains.annotations.NotNull;
import Servicios.UsuarioServices;

public class Encriptacion {

    public static Encriptacion instance = new Encriptacion();

    public static Encriptacion getInstancia(){
        if(instance==null){
            instance = new Encriptacion();
        }
        return instance;
    }

    public void setUserFromCookie(@NotNull Context ctx) {

        String username = (ctx.cookie("COOKIEUSER"));

        System.out.println("Cookie Encriptada Leida: " + username);

        // Desencriptar
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(getInstancia().passwordEncriptacion().toCharArray());
        String decryptedUser = textEncryptor.decrypt(username);

        System.out.println("Cookie Leida: " + decryptedUser);

        Usuario user = UsuarioServices.getInstancia().getUsuarioPorUsername(decryptedUser);

        if (user != null) {
            ctx.sessionAttribute("USUARIO", decryptedUser);
            System.out.println("Usuario logeado automaticamente: " + decryptedUser);
        } else
            System.out.println("No hay usuario en la cookie, No se ha logeado automaticamente");

    }

    public String passwordEncriptacion()
    {
        return "asdf";
    }
}
