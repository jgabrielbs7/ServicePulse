package com.helpdesk.controller;

import com.helpdesk.dto.UsuarioForm;
import com.helpdesk.model.Perfil;
import com.helpdesk.model.Usuario;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.UsuarioService;
import com.helpdesk.util.RegraNegocioException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Gestão de técnicos e permissões. Acesso restrito a ADMIN (ver SecurityConfig). */
@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final UsuarioService service;

    @GetMapping
    public String listar(Model model, Authentication auth) {
        model.addAttribute("usuarios", usuarios.findAllByOrderByNomeAsc());
        model.addAttribute("logado", auth.getName());
        return "admin/usuarios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new UsuarioForm());
        model.addAttribute("perfis", perfisDisponiveis());
        return "admin/usuario-form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        validar(form, null, result);
        if (result.hasErrors()) {
            model.addAttribute("perfis", perfisDisponiveis());
            return "admin/usuario-form";
        }
        Usuario u = service.criar(form);
        redirect.addFlashAttribute("sucesso", "Usuário " + u.getNome() + " cadastrado.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario u = service.buscar(id);
        UsuarioForm form = new UsuarioForm();
        form.setId(u.getId());
        form.setNome(u.getNome());
        form.setEmail(u.getEmail());
        form.setPerfil(u.getPerfil());
        form.setAtivo(Boolean.TRUE.equals(u.getAtivo()));
        model.addAttribute("form", form);
        model.addAttribute("perfis", perfisDisponiveis());
        return "admin/usuario-form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") UsuarioForm form,
                            BindingResult result, Model model, RedirectAttributes redirect, Authentication auth) {
        form.setId(id);
        validar(form, id, result);
        if (!result.hasErrors()) {
            try {
                Usuario u = service.atualizar(id, form, auth.getName());
                redirect.addFlashAttribute("sucesso", "Usuário " + u.getNome() + " atualizado.");
                return "redirect:/admin/usuarios";
            } catch (RegraNegocioException e) {
                result.reject("regra", e.getMessage());
            }
        }
        model.addAttribute("perfis", perfisDisponiveis());
        return "admin/usuario-form";
    }

    @PostMapping("/{id}/ativo")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirect, Authentication auth) {
        try {
            Usuario u = service.alternarAtivo(id, auth.getName());
            redirect.addFlashAttribute("sucesso",
                    "Usuário " + u.getNome() + (Boolean.TRUE.equals(u.getAtivo()) ? " ativado." : " desativado."));
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ---------- Apoio ----------

    private List<Perfil> perfisDisponiveis() {
        return List.of(Perfil.ADMIN, Perfil.TECNICO);
    }

    private void validar(UsuarioForm f, Long id, BindingResult br) {
        if (f.getEmail() != null && !br.hasFieldErrors("email")) {
            boolean duplicado = id == null
                    ? usuarios.existsByEmailIgnoreCase(f.getEmail().trim())
                    : usuarios.existsByEmailIgnoreCaseAndIdNot(f.getEmail().trim(), id);
            if (duplicado) br.rejectValue("email", "duplicado", "Já existe um usuário com este e-mail.");
        }
        String senha = f.getSenha();
        boolean temSenha = senha != null && !senha.isBlank();
        if (id == null && !temSenha) {
            br.rejectValue("senha", "obrigatoria", "Informe a senha.");
        } else if (temSenha) {
            if (senha.length() < 6) {
                br.rejectValue("senha", "curta", "A senha precisa ter pelo menos 6 caracteres.");
            } else if (!senha.equals(f.getConfirmarSenha())) {
                br.rejectValue("confirmarSenha", "diferente", "As senhas não conferem.");
            }
        }
    }
}
