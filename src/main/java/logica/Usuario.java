package logica;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id
    private Long id;

    @Column(name = "username", columnDefinition = "VARCHAR(255)")
    private String username;

    @Column(name = "nombre", columnDefinition = "VARCHAR(255)")
    private String nombre;

    @Column(name = "password", columnDefinition = "VARCHAR(255)")
    private String password;

    @Column(name = "rol", columnDefinition = "VARCHAR(255)")
    private String rol;

    //@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<Sesion> sesiones = new ArrayList<>();

    public Usuario() {
        // Default constructor
    }

    public Usuario(Long id, String username, String nombre, String password, String rol) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.password = password;
        this.rol = rol;
    }
    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
/*
    public List<Sesion> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<Sesion> sesiones) {
        this.sesiones = sesiones;
    }

    public void addSesion(Sesion sesion) {
        sesiones.add(sesion);
        sesion.setUsuario(this);
    }

    public void removeSesion(Sesion sesion) {
        sesiones.remove(sesion);
        sesion.setUsuario(null);
    }*/
}
