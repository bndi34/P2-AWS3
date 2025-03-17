package Servicios;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class GestionDb<T> {
    private static EntityManagerFactory emf;
    protected Class<T> entityClass;

    public GestionDb(Class<T> entityClass) {
        if (emf == null) {
            // Nombre de la unidad de persistencia definida en el persistence.xml
            emf = Persistence.createEntityManagerFactory("exawPersistencia");
        }
        this.entityClass = entityClass;
    }

    protected EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Crea una nueva entidad en la base de datos.
     *
     * @param entidad La entidad a crear.
     * @return La entidad creada.
     * @throws IllegalArgumentException Si la entidad es nula.
     * @throws EntityExistsException    Si la entidad ya existe.
     * @throws PersistenceException     Si ocurre un error de persistencia.
     */
    public T crear(T entidad) throws IllegalArgumentException, EntityExistsException, PersistenceException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entidad);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entidad;
    }

    /**
     * Obtiene el valor del campo anotado con @Id de la entidad.
     *
     * @param entidad La entidad de la cual se obtendrá el valor del campo ID.
     * @return El valor del campo ID.
     */
    private Object getValorCampo(T entidad) {
        if (entidad == null) {
            return null;
        }
        // Aplicando reflexión para obtener el campo con la anotación @Id
        for (Field f : entidad.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                try {
                    f.setAccessible(true); // Acceder a campos privados
                    Object valorCampo = f.get(entidad);

                    System.out.println("Nombre del campo: " + f.getName());
                    System.out.println("Tipo del campo: " + f.getType().getName());
                    System.out.println("Valor del campo: " + valorCampo);

                    return valorCampo;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Edita una entidad existente en la base de datos.
     *
     * @param entidad La entidad a editar.
     * @return La entidad editada.
     * @throws PersistenceException Si ocurre un error de persistencia.
     */
    public T editar(T entidad) throws PersistenceException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            entidad = em.merge(entidad);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entidad;
    }

    /**
     * Elimina una entidad de la base de datos.
     *
     * @param entidadId El ID de la entidad a eliminar.
     * @return true si la entidad fue eliminada correctamente, false en caso contrario.
     * @throws PersistenceException Si ocurre un error de persistencia.
     */
    public boolean eliminar(Object entidadId) throws PersistenceException {
        boolean ok = false;
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T entidad = em.find(entityClass, entidadId);
            if (entidad != null) {
                em.remove(entidad);
                em.getTransaction().commit();
                ok = true;
            }
        } finally {
            em.close();
        }
        return ok;
    }

    /**
     * Busca una entidad por su ID.
     *
     * @param id El ID de la entidad a buscar.
     * @return La entidad encontrada o null si no existe.
     * @throws PersistenceException Si ocurre un error de persistencia.
     */
    public T find(Object id) throws PersistenceException {
        EntityManager em = getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene todas las entidades de la base de datos.
     *
     * @return Una lista con todas las entidades.
     * @throws PersistenceException Si ocurre un error de persistencia.
     */
    public List<T> findAll() throws PersistenceException {
        EntityManager em = getEntityManager();
        try {
            // Crea la consulta Criteria
            CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(entityClass);
            criteriaQuery.select(criteriaQuery.from(entityClass));

            // Ejecuta la consulta y obtén los resultados
            List<T> resultList = em.createQuery(criteriaQuery).getResultList();

            // Si la lista está vacía, devuelve una lista vacía en lugar de null
            return resultList.isEmpty() ? Collections.emptyList() : resultList;
        } catch (Exception e) {
            // Maneja cualquier excepción y devuelve una lista vacía
            System.err.println("Error al ejecutar findAll: " + e.getMessage());
            return Collections.emptyList();
        } finally {
            // Cierra el EntityManager
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}