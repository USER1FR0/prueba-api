package com.proyecto.servicios.service;

import com.proyecto.servicios.model.gestopago.productos.Producto;

import java.util.List;

public interface GestoPagoProductosService {

    List<Producto> obtenerProductos();
}