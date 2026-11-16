package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.CuentaDto;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import cr.ac.ucr.if0009.insecurepay.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final AccesoService acceso;

    public CuentaService(CuentaRepository cuentaRepository, AccesoService acceso) {
        this.cuentaRepository = cuentaRepository;
        this.acceso = acceso;
    }

    public List<CuentaDto> listarTodas(String auth) {
        Usuario usuario = acceso.autenticar(auth);
        return (acceso.puedeAuditar(usuario) ? cuentaRepository.findAll()
                : cuentaRepository.findByUsuarioId(usuario.getId())).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public CuentaDto obtenerPorId(Long id, String auth) {
        Usuario usuario = acceso.autenticar(auth);
        CuentaBancaria cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta bancaria no encontrada."));
        acceso.exigirPropietario(usuario, cuenta.getUsuario().getId(), true);
        return convertirADto(cuenta);
    }

    public List<CuentaDto> obtenerPorUsuario(Long usuarioId, String auth) {
        Usuario usuario = acceso.autenticar(auth);
        acceso.exigirPropietario(usuario, usuarioId, true);
        return cuentaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    private CuentaDto convertirADto(CuentaBancaria cuenta) {
        CuentaDto dto = new CuentaDto();
        dto.setId(cuenta.getId());
        dto.setUsuarioId(cuenta.getUsuario().getId());
        dto.setTitular(cuenta.getUsuario().getNombreCompleto());
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setTipoCuenta(cuenta.getTipoCuenta().name());
        dto.setSaldo(cuenta.getSaldo());
        dto.setMoneda(cuenta.getMoneda());
        dto.setActiva(cuenta.getActiva());
        return dto;
    }
}
