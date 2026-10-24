package com.helpdesk.dto;

import com.helpdesk.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChamadoForm {

    @NotNull(message = "Selecione o cliente.")
    private Long clienteId;

    @NotBlank(message = "Selecione o motivo da abertura.")
    private String motivoAbertura;

    @NotNull(message = "Selecione a prioridade.")
    private Prioridade prioridade = Prioridade.MEDIA;

    /** Técnico que atende o chamado (por padrão, quem está abrindo). */
    private Long tecnicoId;

    @NotBlank(message = "Descreva o problema.")
    @Size(min = 10, max = 4000, message = "Descreva o problema com pelo menos 10 caracteres.")
    private String descricao;
}
