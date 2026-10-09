package com.proyecto.servicios.controller;

import com.proyecto.servicios.entity.mongo.ProductoDocument;
import com.proyecto.servicios.service.CatalogoProductosService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Catalogo de Productos", description = "Sincronizacion y consulta del catalogo de productos GestoPago (almacenado en MongoDB).")
public class GestoPagoProductosController {

    private final CatalogoProductosService catalogoProductosService;

    public GestoPagoProductosController(CatalogoProductosService catalogoProductosService) {
        this.catalogoProductosService = catalogoProductosService;
    }

    @Operation(summary = "Sincronizar catalogo", description = "Trae el catalogo desde GestoPago y lo guarda en Mongo.")
    @PostMapping(value = "/productos/sincronizar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> sincronizar() {
        long total = catalogoProductosService.sincronizar();
        Map<String, Object> respuesta = Map.of(
                "mensaje", "Sincronizacion completada",
                "totalProductos", total);
        return new ResponseEntity<>(respuesta, HttpStatus.OK);
    }

    @Operation(summary = "Consultar catalogo")
    @GetMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ProductoDocument>> consultar() {
        return new ResponseEntity<>(catalogoProductosService.consultar(), HttpStatus.OK);
    }
}
