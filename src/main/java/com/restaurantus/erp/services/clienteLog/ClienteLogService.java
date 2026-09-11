package com.restaurantus.erp.services.clienteLog;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import com.restaurantus.erp.domain.ClienteLog;

public interface ClienteLogService {

    List<ClienteLog> listarClientesLog();

    public void guardarClienteLog(ClienteLog clienteLog);

    public ClienteLog buscarPorId(Long id);

    List<String> getAllDistinctCustomerNames();

    public List<Map<String, Object>> getAllDistinctCustomers();

    Page<ClienteLog> listarClientesPaginados(org.springframework.data.domain.Pageable pageable);

    void eliminarPorId(Long id);
}
