package com.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MotivoForm {

    private Long id;

    @NotBlank(message = "Informe o nome do motivo.")
    @Size(max = 120, message = "Use até 120 caracteres.")
    private String nome;

    private boolean ativo = true;
}
