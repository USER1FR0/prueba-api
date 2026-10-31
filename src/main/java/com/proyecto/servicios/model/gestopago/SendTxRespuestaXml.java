package com.proyecto.servicios.model.gestopago;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 * Respuesta XML de sendTx.do / confirmTx.do (contrato real de GestoPago):
 * <pre>
 * &lt;AUTORIZACION&gt;
 *   &lt;NUM_AUTORIZACION&gt;-1&lt;/NUM_AUTORIZACION&gt;
 *   &lt;CODIGO_MENSAJE&gt;03&lt;/CODIGO_MENSAJE&gt;
 *   &lt;MENSAJE&gt;...&lt;/MENSAJE&gt;
 * &lt;/AUTORIZACION&gt;
 * </pre>
 * La operacion se considera APROBADA cuando NUM_AUTORIZACION es un numero positivo
 * (una autorizacion real); -1 u otro valor significa que NO se aplico.
 */
@Getter
@Setter
@XmlRootElement(name = "AUTORIZACION")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendTxRespuestaXml {

    @XmlElement(name = "NUM_AUTORIZACION")
    private String numAutorizacion;

    @XmlElement(name = "CODIGO_MENSAJE")
    private String codigoMensaje;

    @XmlElement(name = "MENSAJE")
    private String mensaje;

    @XmlElement(name = "ID_TX")
    private String idTx;

    public String codigo() {
        return codigoMensaje != null ? codigoMensaje.trim() : null;
    }

    public String texto() {
        return mensaje != null ? mensaje.trim() : null;
    }

    public String autorizacion() {
        return numAutorizacion != null ? numAutorizacion.trim() : null;
    }

    public String idTransaccion() {
        return idTx != null && !idTx.isBlank() ? idTx.trim() : null;
    }

    /** true solo si hay un numero de autorizacion positivo (operacion aplicada). */
    public boolean aprobada() {
        try {
            return numAutorizacion != null && Long.parseLong(numAutorizacion.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
