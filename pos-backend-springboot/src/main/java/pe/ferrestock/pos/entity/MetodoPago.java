package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "metodo_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo")
    private Long idMetodo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "requiere_codigo")
    private Boolean requiereCodigo;

    @Column(name = "estado")
    private Boolean estado;

    protected MetodoPago() {
    }

    public Long getIdMetodo() {
        return idMetodo;
    }

    public String getNombre() {
        return nombre;
    }

    public Boolean getRequiereCodigo() {
        return requiereCodigo;
    }

    public Boolean getEstado() {
        return estado;
    }
}
