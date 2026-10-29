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
@Table(name = "turno")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long idTurno;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "apertura", nullable = false)
    private LocalDateTime apertura;

    @Column(name = "monto_inicial", nullable = false)
    private BigDecimal montoInicial;

    @Column(name = "cierre")
    private LocalDateTime cierre;

    @Column(name = "monto_esperado", nullable = false)
    private BigDecimal montoEsperado;

    @Column(name = "monto_real")
    private BigDecimal montoReal;

    @Column(name = "diferencia")
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTurno estado;

    protected Turno() {
    }

    public static Turno abrir(Long idUsuario, BigDecimal montoInicial, LocalDateTime apertura) {
        Turno turno = new Turno();
        turno.idUsuario = idUsuario;
        turno.apertura = apertura;
        turno.montoInicial = montoInicial;
        turno.montoEsperado = montoInicial;
        turno.estado = EstadoTurno.ABIERTO;
        return turno;
    }

    public void cerrar(
            BigDecimal montoEsperado,
            BigDecimal montoReal,
            LocalDateTime cierre
    ) {
        this.montoEsperado = montoEsperado;
        this.montoReal = montoReal;
        this.diferencia = montoReal.subtract(montoEsperado);
        this.cierre = cierre;
        this.estado = EstadoTurno.CERRADO;
    }

    public Long getIdTurno() {
        return idTurno;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public LocalDateTime getApertura() {
        return apertura;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public LocalDateTime getCierre() {
        return cierre;
    }

    public BigDecimal getMontoEsperado() {
        return montoEsperado;
    }

    public BigDecimal getMontoReal() {
        return montoReal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public EstadoTurno getEstado() {
        return estado;
    }
}
