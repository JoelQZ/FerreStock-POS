package pe.ferrestock.pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.ferrestock.pos.entity.Producto;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Producto p
            where p.idProducto in :ids
            order by p.idProducto asc
            """)
    List<Producto> bloquearPorIdsOrdenados(@Param("ids") Collection<Long> ids);

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
