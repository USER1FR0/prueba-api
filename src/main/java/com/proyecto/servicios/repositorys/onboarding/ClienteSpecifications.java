package com.proyecto.servicios.repositorys.onboarding;

import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.model.onboarding.request.ClienteFiltroRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Construye una Specification combinando SOLO los filtros presentes.
 * Centraliza la busqueda dinamica fuera del controlador y del service.
 */
public final class ClienteSpecifications {

    private ClienteSpecifications() {
    }

    public static Specification<Cliente> desde(ClienteFiltroRequest f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f.getId() != null) {
                p.add(cb.equal(root.get("id"), f.getId()));
            }
            if (tieneTexto(f.getNombre())) {
                p.add(like(cb, root.get("nombre"), f.getNombre()));
            }
            if (tieneTexto(f.getApellidoPaterno())) {
                p.add(like(cb, root.get("apellidoPaterno"), f.getApellidoPaterno()));
            }
            if (tieneTexto(f.getApellidoMaterno())) {
                p.add(like(cb, root.get("apellidoMaterno"), f.getApellidoMaterno()));
            }
            if (tieneTexto(f.getCurp())) {
                p.add(cb.equal(root.get("curp"), f.getCurp().trim().toUpperCase()));
            }
            if (tieneTexto(f.getRfc())) {
                p.add(cb.equal(root.get("rfc"), f.getRfc().trim().toUpperCase()));
            }
            if (tieneTexto(f.getCorreo())) {
                p.add(cb.equal(cb.lower(root.get("correo")), f.getCorreo().trim().toLowerCase()));
            }
            if (f.getActivo() != null) {
                p.add(cb.equal(root.get("activo"), f.getActivo()));
            }
            if (f.getDesde() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), f.getDesde().atStartOfDay()));
            }
            if (f.getHasta() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), f.getHasta().atTime(LocalTime.MAX)));
            }
            if (tieneTexto(f.getNumeroCuenta())) {
                // EXISTS (cuenta del cliente con ese numero) sin necesitar coleccion mapeada
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Cuenta> cuenta = sub.from(Cuenta.class);
                sub.select(cuenta.get("id")).where(cb.and(
                        cb.equal(cuenta.get("cliente"), root),
                        cb.equal(cuenta.get("numeroCuenta"), f.getNumeroCuenta().trim())));
                p.add(cb.exists(sub));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean tieneTexto(String v) {
        return v != null && !v.isBlank();
    }

    private static Predicate like(CriteriaBuilder cb, Expression<String> campo, String valor) {
        return cb.like(cb.lower(campo), "%" + valor.trim().toLowerCase() + "%");
    }
}
