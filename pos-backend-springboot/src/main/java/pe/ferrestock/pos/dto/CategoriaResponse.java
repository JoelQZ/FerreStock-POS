package pe.ferrestock.pos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pe.ferrestock.pos.entity.Categoria;

public record CategoriaResponse(
        @JsonProperty("id_categoria") Long idCategoria,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("estado") Boolean estado
) {
    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getEstado()
        );
    }
}
