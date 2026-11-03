package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.ferrestock.pos.entity.Negocio;

import java.util.List;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    @Query("select n.igvPorcentaje from Negocio n order by n.id")
    List<java.math.BigDecimal> obtenerPorcentajesIgv();
}
