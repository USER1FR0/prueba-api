package com.proyecto.servicios.mapper;

import com.proyecto.servicios.entity.mongo.ProductoDocument;
import com.proyecto.servicios.model.gestopago.productos.Producto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    ProductoDocument toDocument(Producto producto);

    List<ProductoDocument> toDocuments(List<Producto> productos);
}
