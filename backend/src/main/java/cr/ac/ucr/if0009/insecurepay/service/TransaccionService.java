package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.repository.TransaccionRepository;
import cr.ac.ucr.if0009.insecurepay.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    private final AccesoService acceso;
    private final CuentaService cuentas;

    public TransaccionService(TransaccionRepository transaccionRepository, CuentaRepository cuentaRepository,
            AccesoService acceso, CuentaService cuentas) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.acceso = acceso;
        this.cuentas = cuentas;
    }

    public List<TransaccionDto> listarTodas(String auth) {
        Usuario usuario = acceso.autenticar(auth);
        return transaccionRepository.findAll().stream()
                .filter(t -> puedeLeer(t, usuario))
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public TransaccionDto obtenerPorId(Long id, String auth) {
        Usuario usuario = acceso.autenticar(auth);
        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaccion no encontrada."));
        if (!puedeLeer(transaccion, usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso no autorizado a transaccion ajena.");
        }
        return convertirADto(transaccion);
    }

    public List<TransaccionDto> obtenerPorCuenta(Long cuentaId, String auth) {
        cuentas.obtenerPorId(cuentaId, auth);
        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public List<TransaccionDto> buscarPorConcepto(String termino, String auth) {
        Usuario usuario = acceso.autenticar(auth);
        if (termino == null || termino.isBlank()) return List.of();
        return transaccionRepository.buscarPorTerminoSeguro(termino.trim(), usuario.getId(),
                acceso.puedeAuditar(usuario)).stream().map(this::convertirADto).toList();
    }

    private boolean puedeLeer(Transaccion transaccion, Usuario usuario) {
        return acceso.puedeAuditar(usuario)
                || transaccion.getCuentaOrigen().getUsuario().getId().equals(usuario.getId())
                || transaccion.getCuentaDestino().getUsuario().getId().equals(usuario.getId());
    }

    @Transactional
    public TransaccionDto transferir(TransferenciaDto dto, String auth) {
        Usuario usuario = acceso.autenticar(auth);
        if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El monto debe ser mayor a cero.");
        }

        CuentaBancaria cuentaOrigen = cuentaRepository.findById(dto.getCuentaOrigenId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta de origen no encontrada."));

        acceso.exigirPropietario(usuario, cuentaOrigen.getUsuario().getId(), false);
        CuentaBancaria cuentaDestino = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuentaDestino())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta de destino no encontrada."));

        if (cuentaOrigen.getId().equals(cuentaDestino.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccione otra cuenta de destino.");
        }
        if (cuentaOrigen.getSaldo().compareTo(dto.getMonto()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fondos insuficientes.");
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(dto.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(dto.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMonto(dto.getMonto());
        transaccion.setConceptoHtml(HtmlUtils.htmlEscape(dto.getConceptoHtml() == null ? "" : dto.getConceptoHtml()));
        transaccion.setEstado("COMPLETADA");

        Transaccion guardada = transaccionRepository.save(transaccion);
        return convertirADto(guardada);
    }

    private TransaccionDto convertirADto(Transaccion t) {
        TransaccionDto dto = new TransaccionDto();
        dto.setId(t.getId());
        dto.setCuentaOrigenId(t.getCuentaOrigen().getId());
        dto.setNumeroCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setTitularOrigen(t.getCuentaOrigen().getUsuario().getNombreCompleto());
        dto.setCuentaDestinoId(t.getCuentaDestino().getId());
        dto.setNumeroCuentaDestino(t.getCuentaDestino().getNumeroCuenta());
        dto.setTitularDestino(t.getCuentaDestino().getUsuario().getNombreCompleto());
        dto.setMonto(t.getMonto());
        dto.setConceptoHtml(t.getConceptoHtml());
        dto.setFecha(t.getFecha());
        dto.setEstado(t.getEstado());
        return dto;
    }
}
