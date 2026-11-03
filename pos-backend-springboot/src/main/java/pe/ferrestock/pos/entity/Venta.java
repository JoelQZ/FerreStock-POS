package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "igv", nullable = false)
    private BigDecimal igv;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVenta estado;

    @Column(name = "serie", nullable = false)
    private String serie;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "motivo_anulacion", nullable = false)
    private String motivoAnulacion;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "id_tipo_comprobante", nullable = false)
    private Long idTipoComprobante;

    @Column(name = "id_turno", nullable = false)
    private Long idTurno;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_usuario_anula")
    private Long idUsuarioAnula;

    protected Venta() {
    }

    public static Venta completar(
            LocalDateTime fechaHora,
            BigDecimal subtotal,
            BigDecimal igv,
            BigDecimal total,
            String serie,
            Integer numero,
            Long idCliente,
            Long idTipoComprobante,
            Long idTurno,
            Long idUsuario
    ) {
        Venta venta = new Venta();
        venta.fechaHora = fechaHora;
        venta.subtotal = subtotal;
        venta.igv = igv;
        venta.total = total;
        venta.estado = EstadoVenta.COMPLETADA;
        venta.serie = serie;
        venta.numero = numero;
        venta.motivoAnulacion = "";
        venta.idCliente = idCliente;
        venta.idTipoComprobante = idTipoComprobante;
        venta.idTurno = idTurno;
        venta.idUsuario = idUsuario;
        return venta;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public String getSerie() {
        return serie;
    }

    public Integer getNumero() {
        return numero;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public Long getIdTipoComprobante() {
        return idTipoComprobante;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public Long getIdUsuarioAnula() {
        return idUsuarioAnula;
    }

    public void anular(String motivo, LocalDateTime fecha, Long idUsuario) {
        if (estado != EstadoVenta.COMPLETADA) {
            throw new IllegalStateException("Solo se pueden anular ventas COMPLETADAS");
        }
        estado = EstadoVenta.ANULADA;
        motivoAnulacion = motivo;
        fechaAnulacion = fecha;
        idUsuarioAnula = idUsuario;
    }
}
