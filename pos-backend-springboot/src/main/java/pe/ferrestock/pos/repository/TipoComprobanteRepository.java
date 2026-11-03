package pe.ferrestock.pos.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.ferrestock.pos.entity.TipoComprobante;

import java.util.Optional;

public interface TipoComprobanteRepository extends JpaRepository<TipoComprobante, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TipoComprobante t where t.idTipo = :idTipo")
    Optional<TipoComprobante> bloquearPorId(@Param("idTipo") Long idTipo);
}
