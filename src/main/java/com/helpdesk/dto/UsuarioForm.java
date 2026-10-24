package com.helpdesk.dto;

import com.helpdesk.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UsuarioForm {

    private Long id;

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome pode ter até 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotNull(message = "Selecione o perfil.")
    private Perfil perfil = Perfil.TECNICO;

    private boolean ativo = true;

    /** Obrigatória ao criar; ao editar, só é alterada se preenchida. */
    private String senha;
    private String confirmarSenha;
}
