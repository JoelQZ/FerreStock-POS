package pe.ferrestock.pos.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.ferrestock.pos.dto.AbrirTurnoRequest;
import pe.ferrestock.pos.dto.CerrarTurnoRequest;
import pe.ferrestock.pos.dto.TurnoResumenResponse;
import pe.ferrestock.pos.entity.EstadoTurno;
import pe.ferrestock.pos.entity.Turno;
import pe.ferrestock.pos.repository.TurnoRepository;
import pe.ferrestock.pos.service.MovimientoCajaService;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TurnoServiceImplTest {

    private TurnoRepository turnoRepository;
    private MovimientoCajaService movimientoCajaService;
    private TurnoServiceImpl turnoService;

    @BeforeEach
    void setUp() {
        turnoRepository = mock(TurnoRepository.class);
        movimientoCajaService = mock(MovimientoCajaService.class);
        turnoService = new TurnoServiceImpl(turnoRepository, movimientoCajaService);
    }

    @Test
    void abreTurnoDelUsuarioAutenticadoYCalculaResumenAlCerrar() throws Exception {
        Long idUsuario = 25L;
        Turno turno = Turno.abrir(
                idUsuario,
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 10, 29, 10, 0)
        );
        asignarIdTurno(turno, 11L);

        when(turnoRepository.bloquearUsuario(idUsuario)).thenReturn(idUsuario);
        when(turnoRepository.existsByIdUsuarioAndEstado(idUsuario, EstadoTurno.ABIERTO)).thenReturn(false);
        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocation -> {
            Turno guardado = invocation.getArgument(0);
            asignarIdTurno(guardado, 11L);
            return guardado;
        });
        when(turnoRepository.buscarTurnoAbiertoParaCerrar(idUsuario)).thenReturn(Optional.of(turno));
        when(turnoRepository.sumarVentasEfectivo(11L)).thenReturn(new BigDecimal("25.00"));
        when(turnoRepository.sumarOtrosPagos(11L)).thenReturn(new BigDecimal("50.00"));
        when(movimientoCajaService.obtenerTotales(11L))
                .thenReturn(new MovimientoCajaService.TotalesMovimientoCaja(
                        new BigDecimal("10.00"),
                        new BigDecimal("3.00")
                ));

        var apertura = turnoService.abrir(idUsuario, new AbrirTurnoRequest(new BigDecimal("100.00")));
        TurnoResumenResponse cierre = turnoService.cerrar(
                idUsuario,
                new CerrarTurnoRequest(new BigDecimal("140.00"))
        );

        assertEquals(11L, apertura.idTurno());
        assertEquals("132.00", cierre.montoEsperado().toPlainString());
        assertEquals("25.00", cierre.ventasEfectivo().toPlainString());
        assertEquals("50.00", cierre.otrosPagos().toPlainString());
        assertEquals("10.00", cierre.ingresosCaja().toPlainString());
        assertEquals("3.00", cierre.egresosCaja().toPlainString());
        assertEquals("8.00", cierre.diferencia().toPlainString());
        assertEquals("CERRADO", cierre.estado());
    }

    @Test
    void noPermiteAbrirUnSegundoTurno() {
        Long idUsuario = 25L;
        when(turnoRepository.bloquearUsuario(idUsuario)).thenReturn(idUsuario);
        when(turnoRepository.existsByIdUsuarioAndEstado(idUsuario, EstadoTurno.ABIERTO)).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> turnoService.abrir(idUsuario, new AbrirTurnoRequest(new BigDecimal("100.00")))
        );
        verify(turnoRepository, never()).save(any(Turno.class));
    }

    @Test
    void turnoActualEsNuloCuandoNoHayTurnoAbierto() {
        when(turnoRepository.findFirstByIdUsuarioAndEstadoOrderByAperturaDesc(
                25L,
                EstadoTurno.ABIERTO
        )).thenReturn(Optional.empty());

        assertNull(turnoService.obtenerActual(25L));
    }

    private void asignarIdTurno(Turno turno, Long id) throws Exception {
        Field campo = Turno.class.getDeclaredField("idTurno");
        campo.setAccessible(true);
        campo.set(turno, id);
    }
}
