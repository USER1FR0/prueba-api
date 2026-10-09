package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.mongo.ProductoDocument;
import com.proyecto.servicios.mapper.ProductoMapper;
import com.proyecto.servicios.model.gestopago.productos.Producto;
import com.proyecto.servicios.repositorys.mongo.ProductoRepository;
import com.proyecto.servicios.service.GestoPagoProductosService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoProductosServiceImplTest {

    @Mock
    private GestoPagoProductosService gestoPagoProductosService;

    @Mock
    private ProductoMapper productoMapper;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CatalogoProductosServiceImpl service;

    @Test
    void sincronizar_guardaEnMongoYDevuelveTotal() {
        List<Producto> productos = List.of(new Producto(), new Producto());
        List<ProductoDocument> documentos = List.of(new ProductoDocument(), new ProductoDocument());
        when(gestoPagoProductosService.obtenerProductos()).thenReturn(productos);
        when(productoMapper.toDocuments(productos)).thenReturn(documentos);

        long total = service.sincronizar();

        assertEquals(2, total);
        verify(productoRepository).deleteAll();
        verify(productoRepository).saveAll(documentos);
    }

    @Test
    void consultar_devuelveProductosDeMongo() {
        List<ProductoDocument> documentos = List.of(new ProductoDocument());
        when(productoRepository.findAll()).thenReturn(documentos);

        List<ProductoDocument> resultado = service.consultar();

        assertEquals(1, resultado.size());
    }
}
