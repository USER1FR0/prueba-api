package com.proyecto.servicios.catalogo;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Catalogo de las 32 entidades federativas de Mexico.
 * Se usa para validar el estado del domicilio sin texto libre.
 */
public enum EstadoMexico {
    AGUASCALIENTES,
    BAJA_CALIFORNIA,
    BAJA_CALIFORNIA_SUR,
    CAMPECHE,
    CHIAPAS,
    CHIHUAHUA,
    CIUDAD_DE_MEXICO,
    COAHUILA,
    COLIMA,
    DURANGO,
    GUANAJUATO,
    GUERRERO,
    HIDALGO,
    JALISCO,
    MEXICO,
    MICHOACAN,
    MORELOS,
    NAYARIT,
    NUEVO_LEON,
    OAXACA,
    PUEBLA,
    QUERETARO,
    QUINTANA_ROO,
    SAN_LUIS_POTOSI,
    SINALOA,
    SONORA,
    TABASCO,
    TAMAULIPAS,
    TLAXCALA,
    VERACRUZ,
    YUCATAN,
    ZACATECAS;

    private static final Set<String> NOMBRES =
            Arrays.stream(values()).map(Enum::name).collect(Collectors.toSet());

    public static boolean esValido(String valor) {
        return valor != null && NOMBRES.contains(valor.trim().toUpperCase());
    }
}
