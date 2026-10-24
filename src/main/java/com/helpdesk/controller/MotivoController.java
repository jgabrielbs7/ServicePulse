package com.helpdesk.controller;

import com.helpdesk.dto.MotivoForm;
import com.helpdesk.model.MotivoChamado;
import com.helpdesk.repository.ChamadoRepository;
import com.helpdesk.repository.Contagem;
import com.helpdesk.repository.MotivoChamadoRepository;
import com.helpdesk.service.MotivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/** Gestão dos motivos de abertura de chamado. Acesso restrito a ADMIN (ver SecurityConfig). */
@Controller
@RequestMapping("/admin/motivos")
@RequiredArgsConstructor
public class MotivoController {

    private final MotivoChamadoRepository motivos;
    private final ChamadoRepository chamados;
    private final MotivoService service;

    @GetMapping
    public String listar(Model model) {
        Map<String, Long> usos = new HashMap<>();
        for (Contagem c : chamados.motivosMaisFrequentes(PageRequest.of(0, 1000))) {
            usos.put(c.getNome(), c.getTotal());
        }
        model.addAttribute("motivos", motivos.findAllByOrderByNomeAsc());
        model.addAttribute("usos", usos);
        return "admin/motivos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new MotivoForm());
        return "admin/motivo-form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("form") MotivoForm form, BindingResult result,
                        RedirectAttributes redirect) {
        validarNome(form, null, result);
        if (result.hasErrors()) return "admin/motivo-form";
        MotivoChamado m = service.criar(form);
        redirect.addFlashAttribute("sucesso", "Motivo “" + m.getNome() + "” cadastrado.");
        return "redirect:/admin/motivos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        MotivoChamado m = service.buscar(id);
        MotivoForm form = new MotivoForm();
        form.setId(m.getId());
        form.setNome(m.getNome());
        form.setAtivo(Boolean.TRUE.equals(m.getAtivo()));
        model.addAttribute("form", form);
        return "admin/motivo-form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") MotivoForm form,
                            BindingResult result, RedirectAttributes redirect) {
        form.setId(id);
        validarNome(form, id, result);
        if (result.hasErrors()) return "admin/motivo-form";
        MotivoChamado m = service.atualizar(id, form);
        redirect.addFlashAttribute("sucesso", "Motivo “" + m.getNome() + "” atualizado.");
        return "redirect:/admin/motivos";
    }

    @PostMapping("/{id}/ativo")
    public String alternarAtivo(@PathVariable Long id, RedirectAttributes redirect) {
        MotivoChamado m = service.alternarAtivo(id);
        redirect.addFlashAttribute("sucesso",
                "Motivo “" + m.getNome() + "” " + (Boolean.TRUE.equals(m.getAtivo()) ? "ativado." : "desativado."));
        return "redirect:/admin/motivos";
    }

    private void validarNome(MotivoForm f, Long id, BindingResult br) {
        if (br.hasFieldErrors("nome")) return;
        String nome = f.getNome().trim();
        boolean duplicado = id == null ? motivos.existsByNomeIgnoreCase(nome)
                                       : motivos.existsByNomeIgnoreCaseAndIdNot(nome, id);
        if (duplicado) br.rejectValue("nome", "duplicado", "Já existe um motivo com este nome.");
    }
}
