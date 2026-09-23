package com.proyecto.servicios.controller;

import com.proyecto.servicios.model.gestopago.productos.Producto;
import com.proyecto.servicios.service.GestoPagoProductosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GestoPagoProductosController {

    private final GestoPagoProductosService gestoPagoProductosService;

    public GestoPagoProductosController(GestoPagoProductosService gestoPagoProductosService) {
        this.gestoPagoProductosService = gestoPagoProductosService;
    }

    @GetMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Producto>> obtenerProductos() {
        return new ResponseEntity<>(gestoPagoProductosService.obtenerProductos(), HttpStatus.OK);
    }
}