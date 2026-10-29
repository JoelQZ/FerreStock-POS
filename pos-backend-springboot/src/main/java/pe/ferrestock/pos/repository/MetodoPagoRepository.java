package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferrestock.pos.entity.MetodoPago;

import java.util.List;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    List<MetodoPago> findByEstadoTrue();
}
