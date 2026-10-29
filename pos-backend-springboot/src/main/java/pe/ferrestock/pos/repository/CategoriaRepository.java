package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferrestock.pos.entity.Categoria;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByEstadoTrue();
}
