package pe.ferrestock.pos.dto;

import java.util.Map;

public record ClienteVentaRequest(
        Long idCliente,
        String tipoDocumento,
        String numeroDocumento,
        String nombreRazonSocial,
        String direccion
) {

    public static ClienteVentaRequest from(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number idCliente) {
            return new ClienteVentaRequest(idCliente.longValue(), null, null, null, null);
        }
        if (!(valor instanceof Map<?, ?> datos)) {
            throw new IllegalArgumentException("El cliente debe ser un identificador o un objeto");
        }

        Object idCliente = datos.get("id_cliente");
        return new ClienteVentaRequest(
                idCliente instanceof Number numero ? numero.longValue() : null,
                texto(datos, "tipo_documento"),
                texto(datos, "numero_documento"),
                texto(datos, "nombre_razon_social"),
                texto(datos, "direccion")
        );
    }

    private static String texto(Map<?, ?> datos, String campo) {
        Object valor = datos.get(campo);
        if (valor == null) {
            return null;
        }
        if (valor instanceof String texto) {
            return texto;
        }
        throw new IllegalArgumentException("El campo " + campo + " debe ser texto");
    }
}
