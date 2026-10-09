package com.proyecto.servicios.catalogo;

import java.util.List;

/**
 * Catalogo de codigos postales basado en los rangos oficiales de SEPOMEX
 * (el bloque de CP determina la entidad federativa). Permite validar que un
 * CP exista y que corresponda al estado declarado, sin cargar el padron completo.
 */
public final class CatalogoCodigoPostal {

    private CatalogoCodigoPostal() {
    }

    private record Rango(int min, int max, EstadoMexico estado) {
        boolean contiene(int cp) {
            return cp >= min && cp <= max;
        }
    }

    private static final List<Rango> RANGOS = List.of(
            new Rango(1000, 16999, EstadoMexico.CIUDAD_DE_MEXICO),
            new Rango(20000, 20999, EstadoMexico.AGUASCALIENTES),
            new Rango(21000, 22999, EstadoMexico.BAJA_CALIFORNIA),
            new Rango(23000, 23999, EstadoMexico.BAJA_CALIFORNIA_SUR),
            new Rango(24000, 24999, EstadoMexico.CAMPECHE),
            new Rango(25000, 27999, EstadoMexico.COAHUILA),
            new Rango(28000, 28999, EstadoMexico.COLIMA),
            new Rango(29000, 30999, EstadoMexico.CHIAPAS),
            new Rango(31000, 33999, EstadoMexico.CHIHUAHUA),
            new Rango(34000, 35999, EstadoMexico.DURANGO),
            new Rango(36000, 38999, EstadoMexico.GUANAJUATO),
            new Rango(39000, 41999, EstadoMexico.GUERRERO),
            new Rango(42000, 43999, EstadoMexico.HIDALGO),
            new Rango(44000, 49999, EstadoMexico.JALISCO),
            new Rango(50000, 57999, EstadoMexico.MEXICO),
            new Rango(58000, 61999, EstadoMexico.MICHOACAN),
            new Rango(62000, 62999, EstadoMexico.MORELOS),
            new Rango(63000, 63999, EstadoMexico.NAYARIT),
            new Rango(64000, 67999, EstadoMexico.NUEVO_LEON),
            new Rango(68000, 71999, EstadoMexico.OAXACA),
            new Rango(72000, 75999, EstadoMexico.PUEBLA),
            new Rango(76000, 76999, EstadoMexico.QUERETARO),
            new Rango(77000, 77999, EstadoMexico.QUINTANA_ROO),
            new Rango(78000, 79999, EstadoMexico.SAN_LUIS_POTOSI),
            new Rango(80000, 82999, EstadoMexico.SINALOA),
            new Rango(83000, 85999, EstadoMexico.SONORA),
            new Rango(86000, 86999, EstadoMexico.TABASCO),
            new Rango(87000, 89999, EstadoMexico.TAMAULIPAS),
            new Rango(90000, 90999, EstadoMexico.TLAXCALA),
            new Rango(91000, 96999, EstadoMexico.VERACRUZ),
            new Rango(97000, 97999, EstadoMexico.YUCATAN),
            new Rango(98000, 99999, EstadoMexico.ZACATECAS)
    );

    /** Devuelve el estado al que pertenece el CP, o null si no existe en el catalogo. */
    public static EstadoMexico estadoDe(String codigoPostal) {
        if (codigoPostal == null || !codigoPostal.matches("^[0-9]{5}$")) {
            return null;
        }
        int cp = Integer.parseInt(codigoPostal);
        return RANGOS.stream().filter(r -> r.contiene(cp)).map(Rango::estado).findFirst().orElse(null);
    }

    public static boolean existe(String codigoPostal) {
        return estadoDe(codigoPostal) != null;
    }
}
