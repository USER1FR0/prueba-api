package com.proyecto.servicios.entity.mongo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "productos")
public class ProductoDocument {

    @Id
    private Integer idProducto;

    private String servicio;
    private String producto;
    private Integer idServicio;
    private Integer idCatTipoServicio;
    private Integer tipoFront;
    private Boolean hasDigitoVerificador;
    private Double precio;
    private Boolean showAyuda;
    private String tipoReferencia;
    private String legend;
}
