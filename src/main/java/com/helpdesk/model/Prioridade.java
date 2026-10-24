package com.helpdesk.model;

public enum Prioridade {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String label;

    Prioridade(String label) { this.label = label; }

    public String getLabel() { return label; }
}
