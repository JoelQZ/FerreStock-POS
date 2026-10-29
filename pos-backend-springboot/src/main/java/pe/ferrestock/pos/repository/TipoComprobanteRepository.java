package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferrestock.pos.entity.TipoComprobante;

import java.util.List;

public interface TipoComprobanteRepository extends JpaRepository<TipoComprobante, Long> {
}
