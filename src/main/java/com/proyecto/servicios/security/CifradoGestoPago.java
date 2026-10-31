package com.proyecto.servicios.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Cifra el payload de GestoPago con AES-256-CBC. La llave (32 bytes) y el IV (16 bytes) llegan en
 * Base64 desde variables de entorno; nunca viven en el codigo ni en el repositorio. Si no estan
 * configurados, la app arranca pero no se pueden enviar pagos (no se realiza ningun cargo).
 */
@Component
public class CifradoGestoPago {

    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";
    private static final int BYTES_LLAVE = 32;
    private static final int BYTES_IV = 16;

    private final SecretKeySpec llave;
    private final IvParameterSpec iv;

    public CifradoGestoPago(@Value("${gestopago.cifrado.llave:}") String llaveB64,
                            @Value("${gestopago.cifrado.iv:}") String ivB64) {
        if (esVacio(llaveB64) || esVacio(ivB64)) {
            this.llave = null;
            this.iv = null;
            return;
        }
        byte[] k = Base64.getDecoder().decode(llaveB64.trim());
        byte[] v = Base64.getDecoder().decode(ivB64.trim());
        if (k.length != BYTES_LLAVE || v.length != BYTES_IV) {
            throw new IllegalStateException("GESTOPAGO_CIFRADO_LLAVE debe ser de 32 bytes y el IV de 16 bytes (Base64)");
        }
        this.llave = new SecretKeySpec(k, "AES");
        this.iv = new IvParameterSpec(v);
    }

    public boolean configurado() {
        return llave != null && iv != null;
    }

    /** Devuelve el JSON cifrado en Base64 (valor del parametro {@code signed}). */
    public String cifrar(String json) {
        if (!configurado()) {
            throw new CifradoNoConfiguradoException();
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, llave, iv);
            byte[] cifrado = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible cifrar el payload de GestoPago", e);
        }
    }

    private static boolean esVacio(String v) {
        return v == null || v.isBlank();
    }

    /** Falta la llave o el IV: la peticion no se puede armar ni enviar. */
    public static class CifradoNoConfiguradoException extends IllegalStateException {
        public CifradoNoConfiguradoException() {
            super("No se configuraron la llave y el IV de cifrado de GestoPago");
        }
    }
}
