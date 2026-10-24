package com.helpdesk.util;

public final class Duracao {

    private Duracao() {}

    public static String formatar(Long minutos) {
        if (minutos == null) return "—";
        if (minutos < 60) return minutos + " min";
        long h = minutos / 60;
        long m = minutos % 60;
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }
}
