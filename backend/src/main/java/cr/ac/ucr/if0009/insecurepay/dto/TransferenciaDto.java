package cr.ac.ucr.if0009.insecurepay.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public class TransferenciaDto {

    @NotNull
    @Positive
    private Long cuentaOrigenId;
    @NotBlank
    @Size(max = 30)
    private String numeroCuentaDestino;
    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 13, fraction = 2)
    private BigDecimal monto;
    @Size(max = 2000)
    private String conceptoHtml;

    public TransferenciaDto() {
    }

    public TransferenciaDto(Long cuentaOrigenId, String numeroCuentaDestino, BigDecimal monto, String conceptoHtml) {
        this.cuentaOrigenId = cuentaOrigenId;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.monto = monto;
        this.conceptoHtml = conceptoHtml;
    }

    public Long getCuentaOrigenId() {
        return cuentaOrigenId;
    }

    public void setCuentaOrigenId(Long cuentaOrigenId) {
        this.cuentaOrigenId = cuentaOrigenId;
    }

    public String getNumeroCuentaDestino() {
        return numeroCuentaDestino;
    }

    public void setNumeroCuentaDestino(String numeroCuentaDestino) {
        this.numeroCuentaDestino = numeroCuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getConceptoHtml() {
        return conceptoHtml;
    }

    public void setConceptoHtml(String conceptoHtml) {
        this.conceptoHtml = conceptoHtml;
    }
}
