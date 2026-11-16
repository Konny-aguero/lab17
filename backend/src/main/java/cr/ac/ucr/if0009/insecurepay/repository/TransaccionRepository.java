package cr.ac.ucr.if0009.insecurepay.repository;

import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(Long cuentaOrigenId, Long cuentaDestinoId);

    @Query("""
            SELECT t FROM Transaccion t
            WHERE (LOWER(CAST(t.conceptoHtml AS string)) LIKE LOWER(CONCAT('%', :termino, '%'))
                OR LOWER(t.estado) LIKE LOWER(CONCAT('%', :termino, '%')))
              AND (:auditor = true OR t.cuentaOrigen.usuario.id = :usuarioId
                OR t.cuentaDestino.usuario.id = :usuarioId)
            ORDER BY t.fecha DESC
            """)
    List<Transaccion> buscarPorTerminoSeguro(@Param("termino") String termino,
            @Param("usuarioId") Long usuarioId, @Param("auditor") boolean auditor);
}
