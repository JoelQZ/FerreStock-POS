package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.ferrestock.pos.entity.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
            select p
            from Producto p
            where p.estado = true
              and (
                  :buscar is null
                  or :buscar = ''
                  or lower(p.nombre) like lower(concat('%', :buscar, '%'))
                  or lower(p.codigo) like lower(concat('%', :buscar, '%'))
              )
              and (:categoria is null or p.categoria.idCategoria = :categoria)
            """)
    List<Producto> buscarActivos(@Param("buscar") String buscar, @Param("categoria") Long categoria);
}
