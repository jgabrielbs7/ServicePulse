package com.helpdesk.dto;

import com.helpdesk.model.Prioridade;
import com.helpdesk.model.StatusChamado;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChamadoEdicaoForm {

    @NotNull(message = "Selecione o status.")
    private StatusChamado status;

    @NotNull(message = "Selecione a prioridade.")
    private Prioridade prioridade;

    /** Opcional: chamado pode ficar sem técnico. */
    private Long tecnicoId;

    @Min(value = 0, message = "O tempo não pode ser negativo.")
    private Integer tempoAtendimentoMinutos;
}
