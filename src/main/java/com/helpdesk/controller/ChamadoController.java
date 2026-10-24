package com.helpdesk.controller;

import com.helpdesk.dto.ChamadoEdicaoForm;
import com.helpdesk.dto.ChamadoForm;
import com.helpdesk.model.Chamado;
import com.helpdesk.model.Perfil;
import com.helpdesk.model.Prioridade;
import com.helpdesk.model.StatusChamado;
import com.helpdesk.model.Usuario;
import com.helpdesk.repository.ClienteRepository;
import com.helpdesk.repository.UsuarioRepository;
import com.helpdesk.service.ChamadoService;
import com.helpdesk.service.MotivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService service;
    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final MotivoService motivoService;

    // ---------- Abertura: o técnico abre o chamado em nome do cliente ----------

    @GetMapping("/chamados/novo")
    public String novo(Model model, Authentication auth) {
        ChamadoForm form = new ChamadoForm();
        usuarios.findByEmailIgnoreCase(auth.getName()).map(Usuario::getId).ifPresent(form::setTecnicoId);
        model.addAttribute("form", form);
        carregarCombosNovo(model);
        return "chamados/novo";
    }

    @PostMapping("/chamados")
    public String salvar(@Valid @ModelAttribute("form") ChamadoForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            carregarCombosNovo(model);
            return "chamados/novo";
        }
        Chamado c = service.abrir(form);
        redirect.addFlashAttribute("sucesso", "Chamado " + c.getCodigo() + " aberto com sucesso.");
        return "redirect:/admin/dashboard";
    }

    // ---------- Edição / atribuição ----------

    @GetMapping("/admin/chamados/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Chamado c = service.buscar(id);
        ChamadoEdicaoForm form = new ChamadoEdicaoForm();
        form.setStatus(c.getStatus());
        form.setPrioridade(c.getPrioridade());
        form.setTecnicoId(c.getTecnicoResponsavel() == null ? null : c.getTecnicoResponsavel().getId());
        form.setTempoAtendimentoMinutos(c.getTempoAtendimentoMinutos());
        model.addAttribute("form", form);
        carregarCombosEdicao(model, c);
        return "chamados/editar";
    }

    @PostMapping("/admin/chamados/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("form") ChamadoEdicaoForm form,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            carregarCombosEdicao(model, service.buscar(id));
            return "chamados/editar";
        }
        Chamado c = service.atualizar(id, form);
        redirect.addFlashAttribute("sucesso", "Chamado " + c.getCodigo() + " atualizado.");
        return "redirect:/admin/dashboard";
    }

    // ---------- Apoio ----------

    private List<Usuario> tecnicosAtivos() {
        return usuarios.findByPerfilInAndAtivoTrueOrderByNomeAsc(List.of(Perfil.TECNICO, Perfil.ADMIN));
    }

    private void carregarCombosNovo(Model model) {
        model.addAttribute("clientes", clientes.findAllByOrderByRazaoSocialAsc());
        model.addAttribute("motivos", motivoService.nomesAtivos());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("tecnicos", tecnicosAtivos());
    }

    private void carregarCombosEdicao(Model model, Chamado chamado) {
        model.addAttribute("chamado", chamado);
        model.addAttribute("statusList", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        model.addAttribute("tecnicos", tecnicosAtivos());
    }
}
