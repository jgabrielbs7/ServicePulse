package com.helpdesk.controller;

import com.helpdesk.model.StatusChamado;
import com.helpdesk.service.ChamadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final ChamadoService service;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) StatusChamado status, Model model) {
        model.addAttribute("metricas", service.metricas());
        model.addAttribute("chamados", service.listar(status));
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusList", StatusChamado.values());
        return "admin/dashboard";
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        adicionar(model, "dias", service.volumePorDia(30));
        adicionar(model, "motivos", service.motivosMaisFrequentes(6));
        adicionar(model, "clientes", service.topClientes(5));
        return "admin/analytics";
    }

    private static void adicionar(Model model, String prefixo, Map<String, Long> dados) {
        model.addAttribute(prefixo + "Labels", new ArrayList<>(dados.keySet()));
        model.addAttribute(prefixo + "Valores", new ArrayList<>(dados.values()));
    }
}
