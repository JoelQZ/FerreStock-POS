package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.entity.MovimientoInventario;
import pe.ferrestock.pos.entity.Producto;
import pe.ferrestock.pos.repository.MovimientoInventarioRepository;
import pe.ferrestock.pos.service.MovimientoInventarioService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public MovimientoInventarioServiceImpl(MovimientoInventarioRepository movimientoInventarioRepository) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    @Transactional
    public void registrarSalidas(
            Long idUsuario,
            Long idVenta,
            List<Producto> productosBloqueados,
            Map<Long, BigDecimal> cantidades,
            LocalDateTime fecha
    ) {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        for (Producto producto : productosBloqueados) {
            BigDecimal cantidad = cantidades.get(producto.getIdProducto());
            BigDecimal stockAnterior = producto.getStock();
            producto.descontarStock(cantidad);
            BigDecimal stockNuevo = producto.getStock();
            movimientos.add(MovimientoInventario.salida(
                    producto.getIdProducto(),
                    idUsuario,
                    idVenta,
                    cantidad,
                    stockAnterior,
                    stockNuevo,
                    fecha
            ));
        }
        movimientoInventarioRepository.saveAll(movimientos);
    }
}
