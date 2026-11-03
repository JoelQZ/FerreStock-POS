package pe.ferrestock.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "nombre_razon_social")
    private String nombreRazonSocial;

    @Column(name = "direccion")
    private String direccion;

    protected Cliente() {
    }

    public static Cliente nuevoRuc(String ruc, String nombreRazonSocial, String direccion) {
        Cliente cliente = new Cliente();
        cliente.tipoDocumento = "RUC";
        cliente.numeroDocumento = ruc;
        cliente.nombreRazonSocial = nombreRazonSocial;
        cliente.direccion = direccion == null ? "" : direccion;
        return cliente;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombreRazonSocial() {
        return nombreRazonSocial;
    }

    public String getDireccion() {
        return direccion;
    }
}
