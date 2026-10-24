package com.helpdesk.model;

public enum Perfil {
    ADMIN("Administrador"),
    TECNICO("Técnico"),
    /** Reservado: clientes não acessam o sistema, são apenas cadastros. */
    CLIENTE("Cliente");

    private final String label;

    Perfil(String label) { this.label = label; }

    public String getLabel() { return label; }
}
