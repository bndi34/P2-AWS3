package Servicios;

import logica.Formulario;

import java.util.ArrayList;
import java.util.List;

public class FormularioLocal {

    private static FormularioLocal instancia;

    private List<Formulario> listaFormulario = new ArrayList<>();


    private FormularioLocal() {}

    public static FormularioLocal getInstancia() {
        if (instancia == null) {
            instancia = new FormularioLocal();
        }
        return instancia;
    }

    public Formulario Crear(Formulario formulario){
        listaFormulario.add(formulario);
        return formulario;
    }

    public List<Formulario> listar() {
        // Simulación de una lista de usuarios
        return listaFormulario;
    }

    public boolean eliminando(long idform) {

        for (int i = 0; i < listaFormulario.size(); i++) {
            Formulario form = listaFormulario.get(i);
            // Verificar si el ID del artículo coincide
            if (form.getId() == idform) {
                // Eliminar el artículo de la lista
                listaFormulario.remove(i);
                return true; // Artículo eliminado
            }
        }
        return false;
    }

    public Formulario actualizar(Formulario formulario) {
        // Buscar el usuario por su username
        Formulario tmp = getFormularioPorID(formulario.getId());
        return tmp;
    }

    public Formulario getFormularioPorID(Long id) {
        for (Formulario form : listaFormulario) {
            if (Long.valueOf(form.getId()).equals(id)) {
                return form;
            }
        }
        return null;
    }

    public long generarId() {
        long maxId = 0;
        for (Formulario articulo : listaFormulario) {
            if (articulo.getId() > maxId) {
                maxId = articulo.getId();
            }
        }
        maxId = maxId + FormularioServices.instance.generarIdFormulario();
        return maxId;
    }
}

