package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductosClient;
import com.proyecto.servicios.exception.GestoPagoProductosCommunicationException;
import com.proyecto.servicios.exception.GestoPagoProductosException;
import com.proyecto.servicios.model.gestopago.productos.GetProductListResponse;
import com.proyecto.servicios.model.gestopago.productos.Mensaje;
import com.proyecto.servicios.model.gestopago.productos.Producto;
import com.proyecto.servicios.service.GestoPagoProductosService;
import feign.FeignException;
import feign.RetryableException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GestoPagoProductosServiceImpl implements GestoPagoProductosService {

    private static final String CODIGO_EXITO = "01";

    private static final JAXBContext JAXB_CONTEXT = construirJaxbContext();

    private final GestoPagoProductosClient gestoPagoProductosClient;

    public GestoPagoProductosServiceImpl(GestoPagoProductosClient gestoPagoProductosClient) {
        this.gestoPagoProductosClient = gestoPagoProductosClient;
    }

    @Override
    public List<Producto> obtenerProductos() {
        log.info("Iniciando invocacion a GestoPago getProductList");
        try {
            String xml = gestoPagoProductosClient.getProductList();

            GetProductListResponse response = parsearRespuesta(xml);
            validarRespuesta(response);

            List<Producto> productos = response.getProductos() != null
                    ? response.getProductos()
                    : Collections.emptyList();

            log.info("Invocacion a GestoPago getProductList finalizada: {} productos", productos.size());
            return productos;

        } catch (RetryableException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("Timeout al invocar getProductList: {}", e.getMessage());
                throw new GestoPagoProductosCommunicationException(
                        "Timeout al invocar getProductList", e);
            }
            log.error("Error de comunicacion al invocar getProductList: {}", e.getMessage());
            throw new GestoPagoProductosCommunicationException(
                    "Error de comunicacion al invocar getProductList", e);

        } catch (GestoPagoProductosException e) {
            log.error("Error de integracion en getProductList: {}", e.getMessage());
            throw e;

        } catch (FeignException e) {
            log.error("Error inesperado de Feign en getProductList: {}", e.getMessage());
            throw new GestoPagoProductosException("Error inesperado al invocar getProductList", e);
        }
    }

    private void validarRespuesta(GetProductListResponse response) {
        Mensaje mensaje = response.getMensaje();
        if (mensaje == null || !CODIGO_EXITO.equals(mensaje.getCodigo())) {
            String codigo = (mensaje != null) ? mensaje.getCodigo() : "null";
            throw new GestoPagoProductosException(
                    "GestoPago respondio con codigo no exitoso: " + codigo);
        }
    }

    private GetProductListResponse parsearRespuesta(String xml) {
        try {
            Unmarshaller unmarshaller = JAXB_CONTEXT.createUnmarshaller();
            return (GetProductListResponse) unmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            throw new GestoPagoProductosException(
                    "No se pudo parsear la respuesta XML de GestoPago", e);
        }
    }

    private static JAXBContext construirJaxbContext() {
        try {
            return JAXBContext.newInstance(GetProductListResponse.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("No se pudo inicializar el JAXBContext de productos", e);
        }
    }
}