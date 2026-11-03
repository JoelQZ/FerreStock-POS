package pe.ferrestock.pos.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.ferrestock.pos.dto.ClienteVentaRequest;
import pe.ferrestock.pos.entity.Cliente;
import pe.ferrestock.pos.exception.ResourceNotFoundException;
import pe.ferrestock.pos.repository.ClienteRepository;
import pe.ferrestock.pos.service.ClienteService;

import java.util.regex.Pattern;

@Service
public class ClienteServiceImpl implements ClienteService {

    private static final Pattern RUC_PERUANO = Pattern.compile("\\d{11}");
    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public Cliente resolverParaVenta(ClienteVentaRequest datos, boolean factura) {
        if (datos == null) {
            if (factura) {
                throw new IllegalArgumentException("La factura requiere un cliente con RUC");
            }
            return null;
        }

        if (datos.idCliente() != null) {
            Cliente cliente = clienteRepository.findById(datos.idCliente())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
            if (factura) {
                validarRuc(cliente.getNumeroDocumento());
            }
            return cliente;
        }

        String documento = datos.numeroDocumento();
        if (factura) {
            validarRuc(documento);
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El cliente debe incluir numero_documento");
        }

        return clienteRepository.findByNumeroDocumento(documento)
                .orElseGet(() -> crearClienteFactura(datos, factura, documento));
    }

    private Cliente crearClienteFactura(
            ClienteVentaRequest datos,
            boolean factura,
            String documento
    ) {
        if (!factura) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        if (datos.nombreRazonSocial() == null || datos.nombreRazonSocial().isBlank()) {
            throw new IllegalArgumentException(
                    "El cliente de la factura debe incluir nombre_razon_social"
            );
        }
        return clienteRepository.save(Cliente.nuevoRuc(
                documento,
                datos.nombreRazonSocial(),
                datos.direccion()
        ));
    }

    private void validarRuc(String ruc) {
        if (ruc == null || !RUC_PERUANO.matcher(ruc).matches()) {
            throw new IllegalArgumentException("El RUC debe contener exactamente 11 dígitos");
        }
    }
}
