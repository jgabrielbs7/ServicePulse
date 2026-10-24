package com.helpdesk.service;

import com.helpdesk.dto.MotivoForm;
import com.helpdesk.model.MotivoChamado;
import com.helpdesk.repository.ChamadoRepository;
import com.helpdesk.repository.MotivoChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MotivoService {

    private final MotivoChamadoRepository motivos;
    private final ChamadoRepository chamados;

    @Transactional(readOnly = true)
    public MotivoChamado buscar(Long id) {
        return motivos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motivo não encontrado"));
    }

    /** Nomes exibidos no formulário de novo chamado. */
    @Transactional(readOnly = true)
    public List<String> nomesAtivos() {
        return motivos.findByAtivoTrueOrderByNomeAsc().stream().map(MotivoChamado::getNome).toList();
    }

    @Transactional
    public MotivoChamado criar(MotivoForm f) {
        MotivoChamado m = new MotivoChamado();
        m.setNome(f.getNome().trim());
        m.setAtivo(f.isAtivo());
        return motivos.save(m);
    }

    /** Ao renomear, os chamados existentes acompanham o novo nome (histórico e gráficos continuam coerentes). */
    @Transactional
    public MotivoChamado atualizar(Long id, MotivoForm f) {
        MotivoChamado m = buscar(id);
        String antigo = m.getNome();
        String novo = f.getNome().trim();
        if (!antigo.equals(novo)) {
            chamados.renomearMotivo(antigo, novo);
            m.setNome(novo);
        }
        m.setAtivo(f.isAtivo());
        return motivos.save(m);
    }

    @Transactional
    public MotivoChamado alternarAtivo(Long id) {
        MotivoChamado m = buscar(id);
        m.setAtivo(!Boolean.TRUE.equals(m.getAtivo()));
        return motivos.save(m);
    }
}
