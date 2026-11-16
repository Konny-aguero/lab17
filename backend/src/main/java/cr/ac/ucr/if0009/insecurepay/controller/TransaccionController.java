package cr.ac.ucr.if0009.insecurepay.controller;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.service.TransaccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @GetMapping
    public ResponseEntity<List<TransaccionDto>> listarTodas(@RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(transaccionService.listarTodas(auth));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDto> obtenerPorId(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id, auth));
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionDto>> obtenerPorCuenta(@PathVariable Long cuentaId, @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(transaccionService.obtenerPorCuenta(cuentaId, auth));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<TransaccionDto>> buscarPorConcepto(@RequestParam("termino") String termino, @RequestHeader(value = "Authorization", required = false) String auth) {
        return ResponseEntity.ok(transaccionService.buscarPorConcepto(termino, auth));
    }

    @PostMapping("/transferir")
    public ResponseEntity<TransaccionDto> transferir(@Valid @RequestBody TransferenciaDto dto, @RequestHeader(value = "Authorization", required = false) String auth) {
        TransaccionDto resultado = transaccionService.transferir(dto, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
