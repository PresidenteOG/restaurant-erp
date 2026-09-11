package com.restaurantus.erp.services.clienteLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.restaurantus.erp.dao.mysql.ClienteLogDAO;
import com.restaurantus.erp.domain.ClienteLog;

@Service
public class ClienteLogServiceImpl implements ClienteLogService {

    @Autowired
    private ClienteLogDAO clienteLogDAO;

    @Override
    public List<ClienteLog> listarClientesLog() {
        return clienteLogDAO.findAll();
    }

    @Override
    public void guardarClienteLog(ClienteLog clienteLog) {
        clienteLogDAO.save(clienteLog);
    }

    @Override
    public ClienteLog buscarPorId(Long id) {
        return clienteLogDAO.findById(id).orElse(null);
    }

    @Override
    public List<String> getAllDistinctCustomerNames() {
        return clienteLogDAO.findAllDistinctCustomerNames();
    }

    @Override
    public List<Map<String, Object>> getAllDistinctCustomers() {
        return clienteLogDAO.findAllDistinctCustomerIdsAndNamesAndPhones()
                .stream()
                .map(obj -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("idCliente", String.valueOf(obj[0]));
                    map.put("nombreCliente", obj[1]);
                    map.put("telefono", obj[2]);
                    return map;
                })
                .toList();
    }

    @Override
    public Page<ClienteLog> listarClientesPaginados(Pageable pageable) {
        return clienteLogDAO.findAll(pageable);
    }

    @Override
    public void eliminarPorId(Long id) {
        clienteLogDAO.deleteById(id);
    }

    

}
