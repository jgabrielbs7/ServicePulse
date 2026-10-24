package com.helpdesk.controller;

import com.helpdesk.dto.ClienteForm;
import com.helpdesk.model.Cliente;
import com.helpdesk.repository.ClienteRepository;
import com.helpdesk.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Cadastro de clientes (empresas/pessoas atendidas). Acesso: ADMIN e TECNICO. */
@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clientes;
    private final ClienteService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clientes.findAllByOrderByRazaoSocialAsc());
        return "clientes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ClienteForm());
        return "clientes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") ClienteForm form, BindingResult result,
                        RedirectAttributes redirect) {
        validarDocumento(form, null, result);
        if (result.hasErrors()) return "clientes/form";
        Cliente c = service.criar(form);
        redirect.addFlashAttribute("sucesso", "Cliente " + c.getRazaoSocial() + " cadastrado.");
        return "redirect:/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Cliente c = service.buscar(id);
        ClienteForm form = new ClienteForm();
        form.setId(c.getId());
        form.setRazaoSocial(c.getRazaoSocial());
        form.setCpfCnpj(c.getCpfCnpj());
        form.setEmailContato(c.getEmailContato());
        form.setTelefone(c.getTelefone());
        model.addAttribute("form", form);
        return "clientes/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ClienteForm form,
                            BindingResult result, RedirectAttributes redirect) {
        form.setId(id);
        validarDocumento(form, id, result);
        if (result.hasErrors()) return "clientes/form";
        Cliente c = service.atualizar(id, form);
        redirect.addFlashAttribute("sucesso", "Cliente " + c.getRazaoSocial() + " atualizado.");
        return "redirect:/clientes";
    }

    private void validarDocumento(ClienteForm f, Long id, BindingResult br) {
        if (br.hasFieldErrors("cpfCnpj")) return;
        String formatado = ClienteService.formatarDocumento(f.getCpfCnpj());
        if (formatado == null) {
            br.rejectValue("cpfCnpj", "invalido", "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).");
            return;
        }
        f.setCpfCnpj(formatado);
        boolean duplicado = id == null ? clientes.existsByCpfCnpj(formatado)
                                       : clientes.existsByCpfCnpjAndIdNot(formatado, id);
        if (duplicado) br.rejectValue("cpfCnpj", "duplicado", "Já existe um cliente com este CPF/CNPJ.");
    }
}
