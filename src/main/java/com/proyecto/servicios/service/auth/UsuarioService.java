package com.proyecto.servicios.service.auth;

import com.proyecto.servicios.entity.auth.Usuario;
import com.proyecto.servicios.model.auth.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    /** Crea el usuario de acceso del cliente (1:1). Valida password y unicidad de correo. */
    Usuario crearParaCliente(Long clienteId, String correo, String rawPassword);

    /** Inactiva el usuario asociado al cliente (cascada de baja logica). */
    void inactivarPorCliente(Long clienteId);

    /** Mantiene el correo del usuario en sync cuando cambia el correo del cliente. */
    void sincronizarCorreo(Long clienteId, String nuevoCorreo);

    /** Lista usuarios; si activo es null devuelve todos. */
    List<UsuarioResponse> filtrarPorActivo(Boolean activo);

    /** Activa o inactiva un usuario por id. */
    UsuarioResponse cambiarEstatus(Long id, Boolean activo);
}
