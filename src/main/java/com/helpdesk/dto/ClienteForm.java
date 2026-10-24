package com.helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClienteForm {

    private Long id;

    @NotBlank(message = "Informe a razão social ou o nome.")
    @Size(max = 255, message = "Use até 255 caracteres.")
    private String razaoSocial;

    @NotBlank(message = "Informe o CPF ou CNPJ.")
    private String cpfCnpj;

    @Email(message = "E-mail inválido.")
    private String emailContato;

    @Size(max = 20, message = "Use até 20 caracteres.")
    private String telefone;
}
