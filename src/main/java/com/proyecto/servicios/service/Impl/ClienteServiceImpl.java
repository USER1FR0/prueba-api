package com.proyecto.servicios.service.impl;

import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CodigoPostalInvalidoException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.CurpDuplicadaException;
import com.proyecto.servicios.exception.RfcDuplicadoException;
import com.proyecto.servicios.mapper.ClienteMapper;
import com.proyecto.servicios.model.onboarding.request.ClienteFiltroRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;
import com.proyecto.servicios.repositories.onboarding.ClienteRepository;
import com.proyecto.servicios.repositories.onboarding.ClienteSpecifications;
import com.proyecto.servicios.repositories.onboarding.PaginacionFiltro;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.service.CuentaService;
import com.proyecto.servicios.service.auth.UsuarioService;
import com.proyecto.servicios.service.postal.PostalCodeService;
import com.proyecto.servicios.service.postal.ResultadoValidacionCP;
import com.proyecto.servicios.validation.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final CuentaService cuentaService;
    private final PostalCodeService postalCodeService;
    private final UsuarioService usuarioService;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              ClienteMapper clienteMapper,
                              CuentaService cuentaService,
                              PostalCodeService postalCodeService,
                              UsuarioService usuarioService) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
        this.cuentaService = cuentaService;
        this.postalCodeService = postalCodeService;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {
        // Fail-fast: valida password y unicidad ANTES de persistir nada.
        PasswordValidator.validar(request.getPassword());
        validarUnicidad(request.getCurp(), request.getRfc(), request.getCorreo());
        validarCodigoPostal(request.getDomicilio().getCodigoPostal(), request.getDomicilio().getPais());

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setActivo(true);
        cliente.getDomicilio().setCliente(cliente);

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente registrado con id {}", guardado.getId());

        cuentaService.crearCuentaInicialParaCliente(guardado.getId(), null);
        usuarioService.crearParaCliente(guardado.getId(), guardado.getCorreo(), request.getPassword());

        return construirResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(ClienteFiltroRequest filtro) {
        Pageable pageable = PaginacionFiltro.de(filtro.getPage(), filtro.getSize());
        return clienteRepository.findAll(ClienteSpecifications.desde(filtro), pageable).stream()
                .map(this::construirResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(ClienteUpdateRequest request) {
        Cliente cliente = buscarPorId(request.getId());

        if (request.getCorreo() != null
                && !request.getCorreo().equalsIgnoreCase(cliente.getCorreo())
                && clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new CorreoDuplicadoException("Ya existe un cliente con el correo " + request.getCorreo());
        }

        clienteMapper.actualizarCliente(request, cliente);

        if (request.getDomicilio() != null) {
            validarCodigoPostal(request.getDomicilio().getCodigoPostal(), request.getDomicilio().getPais());
            clienteMapper.actualizarDomicilio(request.getDomicilio(), cliente.getDomicilio());
        }

        Cliente actualizado = clienteRepository.save(cliente);

        if (request.getCorreo() != null) {
            usuarioService.sincronizarCorreo(actualizado.getId(), actualizado.getCorreo());
        }

        return construirResponse(actualizado);
    }

    @Override
    @Transactional
    public void bajaLogica(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
        cuentaService.inactivarCuentasDeCliente(id);
        usuarioService.inactivarPorCliente(id);
        log.info("Cliente {} dado de baja logica", id);
    }

    // ---------- Helpers ----------

    private void validarUnicidad(String curp, String rfc, String correo) {
        if (clienteRepository.existsByCurp(curp)) {
            throw new CurpDuplicadaException("Ya existe un cliente con la CURP " + curp);
        }
        if (clienteRepository.existsByRfc(rfc)) {
            throw new RfcDuplicadoException("Ya existe un cliente con el RFC " + rfc);
        }
        if (clienteRepository.existsByCorreo(correo)) {
            throw new CorreoDuplicadoException("Ya existe un cliente con el correo " + correo);
        }
    }

    private void validarCodigoPostal(String codigoPostal, String pais) {
        if (postalCodeService.validar(codigoPostal, pais) == ResultadoValidacionCP.NO_EXISTE) {
            throw new CodigoPostalInvalidoException(
                    "El codigo postal " + codigoPostal + " no existe para el pais " + pais);
        }
    }

    private Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe el cliente con id " + id));
    }

    private ClienteResponse construirResponse(Cliente cliente) {
        ClienteResponse response = clienteMapper.toResponse(cliente);
        response.setCuentas(cuentaService.consultarPorCliente(cliente.getId()));
        return response;
    }
}
