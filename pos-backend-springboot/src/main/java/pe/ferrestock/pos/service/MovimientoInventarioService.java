package pe.ferrestock.pos.service;

import pe.ferrestock.pos.entity.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MovimientoInventarioService {
    void registrarSalidas(
            Long idUsuario,
            Long idVenta,
            List<Producto> productosBloqueados,
            Map<Long, BigDecimal> cantidades,
            LocalDateTime fecha
    );

    void registrarEntradas(
            Long idUsuario,
            Long idVenta,
            List<Producto> productosBloqueados,
            Map<Long, BigDecimal> cantidades,
            LocalDateTime fecha
    );
}
