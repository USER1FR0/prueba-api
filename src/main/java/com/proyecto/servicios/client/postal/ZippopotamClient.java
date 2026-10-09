package com.proyecto.servicios.client.postal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "zippopotam",
        url = "${postal.zippopotam.url}"
)
public interface ZippopotamClient {

    @GetMapping("/{pais}/{codigoPostal}")
    ZippopotamResponse buscar(@PathVariable("pais") String pais,
                              @PathVariable("codigoPostal") String codigoPostal);
}
