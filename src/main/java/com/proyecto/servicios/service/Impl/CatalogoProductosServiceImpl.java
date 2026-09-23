package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.mongo.ProductoDocument;
import com.proyecto.servicios.mapper.ProductoMapper;
import com.proyecto.servicios.model.gestopago.productos.Producto;
import com.proyecto.servicios.repositorys.mongo.ProductoRepository;
import com.proyecto.servicios.service.CatalogoProductosService;
import com.proyecto.servicios.service.GestoPagoProductosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CatalogoProductosServiceImpl implements CatalogoProductosService {

    private final GestoPagoProductosService gestoPagoProductosService;
    private final ProductoMapper productoMapper;
    private final ProductoRepository productoRepository;

    public CatalogoProductosServiceImpl(GestoPagoProductosService gestoPagoProductosService,
                                        ProductoMapper productoMapper,
                                        ProductoRepository productoRepository) {
        this.gestoPagoProductosService = gestoPagoProductosService;
        this.productoMapper = productoMapper;
        this.productoRepository = productoRepository;
    }

    @Override
    public long sincronizar() {
        log.info("Iniciando sincronizacion del catalogo de productos desde GestoPago");

        List<Producto> productos = gestoPagoProductosService.obtenerProductos();
        List<ProductoDocument> documentos = productoMapper.toDocuments(productos);

        productoRepository.deleteAll();
        productoRepository.saveAll(documentos);

        log.info("Sincronizacion finalizada: {} productos guardados en MongoDB", documentos.size());
        return documentos.size();
    }

    @Override
    public List<ProductoDocument> consultar() {
        List<ProductoDocument> productos = productoRepository.findAll();
        log.info("Consulta de catalogo en MongoDB: {} productos", productos.size());
        return productos;
    }
}
