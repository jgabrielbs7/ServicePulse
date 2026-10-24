package com.helpdesk.service;

import com.helpdesk.dto.ChamadoEdicaoForm;
import com.helpdesk.dto.ChamadoForm;
import com.helpdesk.model.Chamado;
import com.helpdesk.model.Cliente;
import com.helpdesk.model.StatusChamado;
import com.helpdesk.repository.ChamadoRepository;
import com.helpdesk.repository.ClienteRepository;
import com.helpdesk.repository.Contagem;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.util.Duracao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    /** Motivos iniciais: cadastrados no banco na primeira execução. Depois, são gerenciados pela tela de Motivos. */
    public static final List<String> MOTIVOS = List.of(
            "Queda de Link", "Erro no ERP", "Troca de Hardware", "Lentidão na Rede",
            "Problema de Impressora", "Acesso e Senha", "Outros");

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");

    private final ChamadoRepository chamados;
    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;

    public record Metricas(long abertos, long emAndamento, long concluidosHoje, String tempoMedio) {}

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<Chamado> listar(StatusChamado status) {
        return status == null
                ? chamados.findAllByOrderByDataAberturaDesc()
                : chamados.findByStatusOrderByDataAberturaDesc(status);
    }

    @Transactional(readOnly = true)
    public Chamado buscar(Long id) {
        return chamados.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado não encontrado"));
    }

    @Transactional(readOnly = true)
    public Metricas metricas() {
        LocalDate hoje = LocalDate.now();
        long concluidosHoje = chamados.countByStatusAndDataFechamentoBetween(
                StatusChamado.FECHADO, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
        Double media = chamados.mediaTempoAtendimento(StatusChamado.FECHADO);
        return new Metricas(
                chamados.countByStatus(StatusChamado.ABERTO),
                chamados.countByStatus(StatusChamado.EM_ANDAMENTO),
                concluidosHoje,
                Duracao.formatar(media == null ? null : Math.round(media)));
    }

    // ---------- Escrita ----------

    @Transactional
    public Chamado abrir(ChamadoForm form) {
        Cliente cliente = clientes.findById(form.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente inválido"));
        Chamado c = new Chamado();
        c.setCliente(cliente);
        c.setMotivoAbertura(form.getMotivoAbertura());
        c.setDescricao(form.getDescricao().trim());
        c.setPrioridade(form.getPrioridade());
        c.setStatus(StatusChamado.ABERTO);
        c.setDataAbertura(LocalDateTime.now());
        if (form.getTecnicoId() != null) {
            c.setTecnicoResponsavel(usuarios.findById(form.getTecnicoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico inválido")));
        }
        return chamados.save(c);
    }

    @Transactional
    public Chamado atualizar(Long id, ChamadoEdicaoForm form) {
        Chamado c = buscar(id);
        c.setPrioridade(form.getPrioridade());
        c.setTecnicoResponsavel(form.getTecnicoId() == null ? null
                : usuarios.findById(form.getTecnicoId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico inválido")));
        c.setStatus(form.getStatus());
        if (form.getTempoAtendimentoMinutos() != null) {
            c.setTempoAtendimentoMinutos(form.getTempoAtendimentoMinutos());
        }

        if (form.getStatus() == StatusChamado.FECHADO) {
            if (c.getDataFechamento() == null) c.setDataFechamento(LocalDateTime.now());
            if (c.getTempoAtendimentoMinutos() == null) {
                long min = Duration.between(c.getDataAbertura(), c.getDataFechamento()).toMinutes();
                c.setTempoAtendimentoMinutos((int) Math.max(min, 0));
            }
        } else {
            c.setDataFechamento(null);
        }
        return chamados.save(c);
    }

    // ---------- Analytics ----------

    /** Volume diário nos últimos {@code dias} dias, incluindo dias sem chamados (valor 0). */
    @Transactional(readOnly = true)
    public Map<String, Long> volumePorDia(int dias) {
        LocalDate inicio = LocalDate.now().minusDays(dias - 1L);
        Map<String, Long> bruto = chamados.volumePorDia(inicio.atStartOfDay()).stream()
                .collect(Collectors.toMap(r -> r.getDia(), r -> r.getTotal()));
        Map<String, Long> resultado = new LinkedHashMap<>();
        for (int i = 0; i < dias; i++) {
            LocalDate d = inicio.plusDays(i);
            resultado.put(d.format(DIA_MES), bruto.getOrDefault(d.toString(), 0L));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> motivosMaisFrequentes(int limite) {
        return paraMapa(chamados.motivosMaisFrequentes(PageRequest.of(0, limite)));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> topClientes(int limite) {
        return paraMapa(chamados.topClientes(PageRequest.of(0, limite)));
    }

    private static Map<String, Long> paraMapa(List<Contagem> lista) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        lista.forEach(c -> mapa.put(c.getNome(), c.getTotal()));
        return mapa;
    }
}
