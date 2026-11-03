package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "negocio")
public class Negocio {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "igv_porcentaje")
    private BigDecimal igvPorcentaje;

    protected Negocio() {
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getIgvPorcentaje() {
        return igvPorcentaje;
    }
}
