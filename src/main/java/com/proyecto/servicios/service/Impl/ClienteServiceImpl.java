package com.proyecto.servicios.service.Impl;

import com.proyecto.servicios.entity.onboarding.Cliente;
import com.proyecto.servicios.entity.onboarding.Cuenta;
import com.proyecto.servicios.entity.onboarding.Domicilio;
import com.proyecto.servicios.exception.ClienteNoEncontradoException;
import com.proyecto.servicios.exception.CorreoDuplicadoException;
import com.proyecto.servicios.exception.CodigoPostalInvalidoException;
import com.proyecto.servicios.exception.CurpDuplicadaException;
import com.proyecto.servicios.exception.RfcDuplicadoException;
import com.proyecto.servicios.mapper.ClienteMapper;
import com.proyecto.servicios.model.onboarding.request.ClienteRequest;
import com.proyecto.servicios.model.onboarding.request.ClienteUpdateRequest;
import com.proyecto.servicios.model.onboarding.response.ClienteResponse;
import com.proyecto.servicios.repositorys.onboarding.ClienteRepository;
import com.proyecto.servicios.service.ClienteService;
import com.proyecto.servicios.service.CuentaService;
import com.proyecto.servicios.service.auth.UsuarioService;
import com.proyecto.servicios.service.postal.PostalCodeService;
import com.proyecto.servicios.service.postal.ResultadoValidacionCP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        validarUnicidad(request.getCurp(), request.getRfc(), request.getCorreo());
        validarCodigoPostal(request.getDomicilio().getCodigoPostal(), request.getDomicilio().getPais());

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setActivo(true);

        Domicilio domicilio = cliente.getDomicilio();
        domicilio.setCliente(cliente);

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente registrado con id {}", guardado.getId());

        cuentaService.crearCuentaParaCliente(guardado, null);

        // Usuario de acceso 1:1 (correo del cliente como login, password cifrado)
        usuarioService.crearParaCliente(guardado.getId(), guardado.getCorreo(), request.getPassword());

        return construirResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarTodos() {
        return clienteRepository.findAll().stream().map(clienteMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorId(Long id) {
        return construirResponse(buscarPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCurp(String curp) {
        Cliente cliente = clienteRepository.findByCurp(curp)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con CURP " + curp));
        return construirResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorRfc(String rfc) {
        Cliente cliente = clienteRepository.findByRfc(rfc)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con RFC " + rfc));
        return construirResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorCorreo(String correo) {
        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe un cliente con correo " + correo));
        return construirResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse consultarPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaService.obtenerEntidadPorNumero(numeroCuenta);
        return construirResponse(cuenta.getCliente());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorNombre(String nombre) {
        return mapear(clienteRepository.findByNombreContainingIgnoreCase(nombre));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorApellidoPaterno(String apellidoPaterno) {
        return mapear(clienteRepository.findByApellidoPaternoContainingIgnoreCase(apellidoPaterno));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorApellidoMaterno(String apellidoMaterno) {
        return mapear(clienteRepository.findByApellidoMaternoContainingIgnoreCase(apellidoMaterno));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarActivos() {
        return mapear(clienteRepository.findByActivoTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> consultarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);
        return mapear(clienteRepository.findByFechaCreacionBetween(inicio, fin));
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Long id, ClienteUpdateRequest request) {
        Cliente cliente = buscarPorId(id);

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

        // Mantener el correo del usuario de acceso en sync si cambio
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
        ResultadoValidacionCP resultado = postalCodeService.validar(codigoPostal, pais);
        if (resultado == ResultadoValidacionCP.NO_EXISTE) {
            throw new CodigoPostalInvalidoException(
                    "El codigo postal " + codigoPostal + " no existe para el pais " + pais);
        }
        // NO_VERIFICABLE: la API no respondio, se permite el alta (fail-open).
    }

    private Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException("No existe el cliente con id " + id));
    }

    private List<ClienteResponse> mapear(List<Cliente> clientes) {
        return clientes.stream().map(clienteMapper::toResponse).toList();
    }

    private ClienteResponse construirResponse(Cliente cliente) {
        ClienteResponse response = clienteMapper.toResponse(cliente);
        response.setCuentas(cuentaService.consultarPorCliente(cliente.getId()));
        return response;
    }
}
