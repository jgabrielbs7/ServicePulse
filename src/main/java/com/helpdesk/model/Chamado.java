package com.helpdesk.model;

import com.helpdesk.util.Duracao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_chamado")
@Getter @Setter @NoArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "motivo_abertura", nullable = false, length = 120)
    private String motivoAbertura;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusChamado status = StatusChamado.ABERTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade = Prioridade.MEDIA;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura = LocalDateTime.now();

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    /** Tempo total gasto na resolução, em minutos. */
    @Column(name = "tempo_atendimento_minutos")
    private Integer tempoAtendimentoMinutos;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnicoResponsavel;

    @PrePersist
    void aoCriar() {
        if (dataAbertura == null) dataAbertura = LocalDateTime.now();
    }

    public String getCodigo() {
        return String.format("CH-%05d", id == null ? 0 : id);
    }

    public String getTempoFormatado() {
        return Duracao.formatar(tempoAtendimentoMinutos == null ? null : tempoAtendimentoMinutos.longValue());
    }
}
