package com.proyecto.servicios.client.postal;

import com.proyecto.servicios.config.ZipcodestackClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "zipcodestack",
        url = "${postal.zipcodestack.url}",
        configuration = ZipcodestackClientConfig.class
)
public interface ZipcodestackClient {

    @GetMapping("/v1/search")
    ZipcodestackResponse buscar(@RequestParam("codes") String codigoPostal,
                                @RequestParam("country") String pais);
}
