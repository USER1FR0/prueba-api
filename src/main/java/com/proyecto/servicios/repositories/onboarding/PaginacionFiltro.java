package com.proyecto.servicios.repositories.onboarding;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Convierte parametros page/size de un filtro a Pageable, aplicando defaults y topes.
 * Centraliza la regla para que todos los buscadores paginen igual (DRY).
 */
public final class PaginacionFiltro {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = 200;

    private PaginacionFiltro() {
    }

    public static Pageable de(Integer page, Integer size) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(p, s);
    }
}
