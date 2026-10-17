package com.proyecto.servicios.repositories.onboarding;

import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.model.onboarding.request.CuentaFiltroRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Construye una Specification combinando SOLO los filtros presentes.
 * Mismo patron que ClienteSpecifications para mantener consistencia.
 */
public final class CuentaSpecifications {

    private CuentaSpecifications() {
    }

    public static Specification<Cuenta> desde(CuentaFiltroRequest f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (tieneTexto(f.getNumeroCuenta())) {
                p.add(cb.equal(root.get("numeroCuenta"), f.getNumeroCuenta().trim()));
            }
            if (f.getClienteId() != null) {
                p.add(cb.equal(root.get("cliente").get("id"), f.getClienteId()));
            }
            if (f.getEstatus() != null) {
                p.add(cb.equal(root.get("estatus"), f.getEstatus()));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean tieneTexto(String v) {
        return v != null && !v.isBlank();
    }
}
