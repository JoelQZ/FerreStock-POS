package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Cliente;

public record ClienteResponse(
        @JsonProperty("id_cliente") Long idCliente,
        @JsonProperty("tipo_documento") String tipoDocumento,
        @JsonProperty("numero_documento") String numeroDocumento,
        @JsonProperty("nombre_razon_social") String nombreRazonSocial,
        @JsonProperty("direccion") String direccion
) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getIdCliente(),
                cliente.getTipoDocumento(),
                cliente.getNumeroDocumento(),
                cliente.getNombreRazonSocial(),
                cliente.getDireccion()
        );
    }
}
