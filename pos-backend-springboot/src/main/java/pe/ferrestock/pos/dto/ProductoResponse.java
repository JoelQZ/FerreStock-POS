package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        @JsonProperty("id_producto") Long idProducto,
        @JsonProperty("codigo") String codigo,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("precio_venta") BigDecimal precioVenta,
        @JsonProperty("stock") BigDecimal stock,
        @JsonProperty("stock_minimo") BigDecimal stockMinimo,
        @JsonProperty("estado") Boolean estado,
        @JsonProperty("categoria") Long categoria,
        @JsonProperty("marca") Long marca,
        @JsonProperty("unidad_medida") Long unidadMedida
) {
    public static ProductoResponse from(Producto producto) {
        return new ProductoResponse(
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecioVenta(),
                producto.getStock(),
                producto.getStockMinimo(),
                producto.getEstado(),
                producto.getCategoria() == null ? null : producto.getCategoria().getIdCategoria(),
                producto.getMarca() == null ? null : producto.getMarca().getIdMarca(),
                producto.getUnidadMedida() == null ? null : producto.getUnidadMedida().getIdUnidad()
        );
    }
}
