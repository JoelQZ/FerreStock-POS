package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.ferrestock.pos.entity.EstadoTurno;
import pe.ferrestock.pos.entity.Turno;

import java.math.BigDecimal;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    @Query(value = "SELECT id FROM usuario WHERE id = :usuarioId FOR UPDATE", nativeQuery = true)
    Long bloquearUsuario(@Param("usuarioId") Long usuarioId);

    boolean existsByIdUsuarioAndEstado(Long idUsuario, EstadoTurno estado);

    Optional<Turno> findFirstByIdUsuarioAndEstadoOrderByAperturaDesc(
            Long idUsuario,
            EstadoTurno estado
    );

    Optional<Turno> findByIdTurnoAndIdUsuarioAndEstado(
            Long idTurno,
            Long idUsuario,
            EstadoTurno estado
    );

    @Query(value = """
            SELECT *
            FROM turno
            WHERE id_usuario = :usuarioId AND estado = 'ABIERTO'
            ORDER BY apertura DESC
            LIMIT 1
            FOR UPDATE
            """, nativeQuery = true)
    Optional<Turno> buscarTurnoAbiertoParaCerrar(@Param("usuarioId") Long usuarioId);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE WHEN LOWER(TRIM(mp.nombre)) = 'efectivo' THEN p.monto ELSE 0 END
            ), 0)
            FROM pago p
            JOIN venta v ON v.id_venta = p.id_venta
            JOIN metodo_pago mp ON mp.id_metodo = p.id_metodo
            WHERE v.id_turno = :turnoId AND v.estado = 'COMPLETADA'
            """, nativeQuery = true)
    BigDecimal sumarVentasEfectivo(@Param("turnoId") Long turnoId);

    @Query(value = """
            SELECT COALESCE(SUM(
                CASE WHEN LOWER(TRIM(mp.nombre)) <> 'efectivo' THEN p.monto ELSE 0 END
            ), 0)
            FROM pago p
            JOIN venta v ON v.id_venta = p.id_venta
            JOIN metodo_pago mp ON mp.id_metodo = p.id_metodo
            WHERE v.id_turno = :turnoId AND v.estado = 'COMPLETADA'
            """, nativeQuery = true)
    BigDecimal sumarOtrosPagos(@Param("turnoId") Long turnoId);
}
