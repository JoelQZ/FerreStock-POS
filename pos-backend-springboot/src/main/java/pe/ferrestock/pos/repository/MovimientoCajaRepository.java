package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.ferrestock.pos.entity.MovimientoCaja;

import java.math.BigDecimal;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    @Query(value = """
            SELECT COALESCE(SUM(m.monto), 0)
            FROM movimiento_caja m
            WHERE m.id_turno = :turnoId AND m.tipo = :tipo
            """, nativeQuery = true)
    BigDecimal sumarPorTurnoYTipo(
            @Param("turnoId") Long turnoId,
            @Param("tipo") String tipo
    );
}
