package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "id_venta", nullable = false)
    private Long idVenta;

    @Column(name = "id_metodo", nullable = false)
    private Long idMetodo;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "monto_recibido", nullable = false)
    private BigDecimal montoRecibido;

    @Column(name = "vuelto", nullable = false)
    private BigDecimal vuelto;

    @Column(name = "codigo_operacion", nullable = false)
    private String codigoOperacion;

    protected Pago() {
    }

    public static Pago crear(
            Long idVenta,
            Long idMetodo,
            BigDecimal monto,
            BigDecimal montoRecibido,
            BigDecimal vuelto,
            String codigoOperacion
    ) {
        Pago pago = new Pago();
        pago.idVenta = idVenta;
        pago.idMetodo = idMetodo;
        pago.monto = monto;
        pago.montoRecibido = montoRecibido;
        pago.vuelto = vuelto;
        pago.codigoOperacion = codigoOperacion == null ? "" : codigoOperacion;
        return pago;
    }

    public Long getIdPago() {
        return idPago;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public Long getIdMetodo() {
        return idMetodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public BigDecimal getVuelto() {
        return vuelto;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }
}
