package com.proyecto.servicios.service;

import com.proyecto.servicios.entity.mongo.ProductoDocument;

import java.util.List;

public interface CatalogoProductosService {

    long sincronizar();

    List<ProductoDocument> consultar();
}
