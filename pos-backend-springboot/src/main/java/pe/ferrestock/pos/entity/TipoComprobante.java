package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipo_comprobante")
public class TipoComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "serie")
    private String serie;

    @Column(name = "correlativo_actual")
    private Integer correlativoActual;

    protected TipoComprobante() {
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSerie() {
        return serie;
    }

    public Integer getCorrelativoActual() {
        return correlativoActual;
    }
}
