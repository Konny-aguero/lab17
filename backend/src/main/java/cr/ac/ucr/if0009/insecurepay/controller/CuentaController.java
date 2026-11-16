package cr.ac.ucr.if0009.insecurepay.controller;

import cr.ac.ucr.if0009.insecurepay.dto.CuentaDto;
import cr.ac.ucr.if0009.insecurepay.service.CuentaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {
    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public List<CuentaDto> listarTodas(@RequestHeader(value = "Authorization", required = false) String auth) {
        return cuentaService.listarTodas(auth);
    }

    @GetMapping("/{id}")
    public CuentaDto obtenerPorId(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return cuentaService.obtenerPorId(id, auth);
    }

    @GetMapping("/{id}/saldo")
    public Map<String, Object> obtenerSaldo(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        CuentaDto cuenta = cuentaService.obtenerPorId(id, auth);
        return Map.of("cuentaId", cuenta.getId(), "numeroCuenta", cuenta.getNumeroCuenta(),
                "saldo", cuenta.getSaldo(), "moneda", cuenta.getMoneda());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<CuentaDto> obtenerPorUsuario(@PathVariable Long usuarioId,
            @RequestHeader(value = "Authorization", required = false) String auth) {
        return cuentaService.obtenerPorUsuario(usuarioId, auth);
    }
}
