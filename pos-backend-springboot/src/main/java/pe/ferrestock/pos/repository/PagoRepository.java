package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferrestock.pos.entity.Pago;

import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findAllByIdVentaOrderByIdPagoAsc(Long idVenta);
}
