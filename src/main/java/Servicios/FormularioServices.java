package Servicios;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import logica.Formulario;

public class FormularioServices extends GestionDb<Formulario> {

    public static FormularioServices instance;

    public FormularioServices() {
        super(Formulario.class);
    }

    public static FormularioServices getInstancia() {
        if (instance == null) {
            instance = new FormularioServices();
        }
        return instance;
    }


    public long generarIdFormulario() {
        EntityManager em = getEntityManager();
        try {
            // Consulta para obtener el máximo id
            TypedQuery<Long> query = em.createQuery(
                    "SELECT MAX(f.id) FROM Formulario f", Long.class);

            // Obtener el resultado de la consulta
            Long maxId = query.getSingleResult();

            // Si no hay formularios, comenzar desde 1
            return (maxId != null) ? maxId + 1 : 1;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
