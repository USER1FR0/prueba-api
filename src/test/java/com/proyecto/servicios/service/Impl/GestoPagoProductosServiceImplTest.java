package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.client.GestoPagoProductosClient;
import com.proyecto.servicios.exception.GestoPagoProductosAuthException;
import com.proyecto.servicios.exception.GestoPagoProductosCommunicationException;
import com.proyecto.servicios.exception.GestoPagoProductosException;
import com.proyecto.servicios.model.gestopago.productos.Producto;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestoPagoProductosServiceImplTest {

    @Mock
    private GestoPagoProductosClient client;

    @InjectMocks
    private GestoPagoProductosServiceImpl service;

    private static final String XML_OK = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
              <MENSAJE><CODIGO>01</CODIGO><TEXTO>Operacion realizada con exito</TEXTO></MENSAJE>
              <PRODUCTOS>
                <producto servicio='ABIB' producto='ABIB 100' idServicio='2284' idProducto='14302' idCatTipoServicio='13' tipoFront='1' hasDigitoVerificador='false' precio='100.0' showAyuda='false' tipoReferencia='a'><legend><![CDATA[texto]]></legend></producto>
                <producto servicio='Amazon' producto='Amazon 100' idServicio='71' idProducto='200' idCatTipoServicio='10' tipoFront='1' hasDigitoVerificador='false' precio='100.0' showAyuda='false' tipoReferencia='a'><legend><![CDATA[texto2]]></legend></producto>
              </PRODUCTOS>
            </RESPONSE>
            """;

    private static final String XML_CODIGO_ERROR = """
            <?xml version='1.0' encoding='UTF-8'?>
            <RESPONSE>
              <MENSAJE><CODIGO>99</CODIGO><TEXTO>Error</TEXTO></MENSAJE>
              <PRODUCTOS></PRODUCTOS>
            </RESPONSE>
            """;

    @Test
    void obtenerProductos_respuestaExitosa_devuelveLista() {
        when(client.getProductList()).thenReturn(XML_OK);

        List<Producto> productos = service.obtenerProductos();

        assertEquals(2, productos.size());
        assertEquals("ABIB", productos.get(0).getServicio());
        assertEquals(14302, productos.get(0).getIdProducto().intValue());
        assertEquals(100.0, productos.get(0).getPrecio(), 0.001);
    }

    @Test
    void obtenerProductos_codigoNoExitoso_lanzaExcepcion() {
        when(client.getProductList()).thenReturn(XML_CODIGO_ERROR);

        assertThrows(GestoPagoProductosException.class, () -> service.obtenerProductos());
    }

    @Test
    void obtenerProductos_timeout_lanzaCommunicationException() {
        RetryableException retryable = mock(RetryableException.class);
        when(retryable.getCause()).thenReturn(new SocketTimeoutException("timeout"));
        when(client.getProductList()).thenThrow(retryable);

        assertThrows(GestoPagoProductosCommunicationException.class,
                () -> service.obtenerProductos());
    }

    @Test
    void obtenerProductos_errorComunicacion_lanzaCommunicationException() {
        RetryableException retryable = mock(RetryableException.class);
        when(retryable.getCause()).thenReturn(new ConnectException("refused"));
        when(client.getProductList()).thenThrow(retryable);

        assertThrows(GestoPagoProductosCommunicationException.class,
                () -> service.obtenerProductos());
    }

    @Test
    void obtenerProductos_errorAuth_propagaExcepcion() {
        when(client.getProductList()).thenThrow(new GestoPagoProductosAuthException("401"));

        assertThrows(GestoPagoProductosAuthException.class,
                () -> service.obtenerProductos());
    }
}
