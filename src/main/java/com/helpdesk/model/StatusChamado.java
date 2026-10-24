package com.helpdesk.model;

public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ANDAMENTO("Em andamento"),
    AGUARDANDO_CLIENTE("Aguardando cliente"),
    FECHADO("Fechado");

    private final String label;

    StatusChamado(String label) { this.label = label; }

    public String getLabel() { return label; }
}
