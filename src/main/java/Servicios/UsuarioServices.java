package Servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import logica.Usuario;

public class UsuarioServices extends GestionDb<Usuario> {

    public static UsuarioServices instance;

    public UsuarioServices() {
        super(Usuario.class);
    }

    public static UsuarioServices getInstancia() {
        if (instance == null) {
            instance = new UsuarioServices();
        }
        return instance;
    }

    public Usuario getUsuarioPorUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class); // Use "Usuario" instead of "USUARIO"
            query.setParameter("username", username);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long generarIdUsuario() {
        EntityManager em = getEntityManager();
        try {
            // Consulta para obtener el máximo id
            TypedQuery<Long> query = em.createQuery(
                    "SELECT MAX(a.id) FROM Usuario a", Long.class);

            // Obtener el resultado de la consulta
            Long maxId = query.getSingleResult();

            // Si no hay artículos, comenzar desde 1
            return (maxId != null) ? maxId + 1 : 1;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
