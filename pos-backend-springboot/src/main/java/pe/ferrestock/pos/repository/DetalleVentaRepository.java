package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferrestock.pos.entity.DetalleVenta;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findAllByIdVentaOrderByIdDetalleAsc(Long idVenta);
}
