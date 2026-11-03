package pe.ferrestock.pos.service;

import pe.ferrestock.pos.dto.ClienteVentaRequest;
import pe.ferrestock.pos.entity.Cliente;

public interface ClienteService {
    Cliente resolverParaVenta(ClienteVentaRequest cliente, boolean factura);
}
