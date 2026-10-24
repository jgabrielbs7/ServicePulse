package com.helpdesk.service;

import com.helpdesk.dto.ClienteForm;
import com.helpdesk.model.Cliente;
import com.helpdesk.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clientes;

    @Transactional(readOnly = true)
    public Cliente buscar(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    @Transactional
    public Cliente criar(ClienteForm f) {
        return clientes.save(preencher(new Cliente(), f));
    }

    @Transactional
    public Cliente atualizar(Long id, ClienteForm f) {
        return clientes.save(preencher(buscar(id), f));
    }

    private Cliente preencher(Cliente c, ClienteForm f) {
        c.setRazaoSocial(f.getRazaoSocial().trim());
        c.setCpfCnpj(f.getCpfCnpj());
        c.setEmailContato(vazioParaNulo(f.getEmailContato()));
        c.setTelefone(vazioParaNulo(f.getTelefone()));
        return c;
    }

    private static String vazioParaNulo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    /** Normaliza CPF (11 dígitos) ou CNPJ (14 dígitos). Retorna null se o tamanho for inválido. */
    public static String formatarDocumento(String valor) {
        String d = valor == null ? "" : valor.replaceAll("\\D", "");
        if (d.length() == 11) {
            return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
        }
        if (d.length() == 14) {
            return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/"
                    + d.substring(8, 12) + "-" + d.substring(12);
        }
        return null;
    }
}
