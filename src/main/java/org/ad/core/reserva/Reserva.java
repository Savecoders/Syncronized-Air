package org.ad.core.reserva;

import org.ad.core.users.User;

public class Reserva {

    private User usuario;
    private int asiento;

    public Reserva(User usuario, int asiento) {
        this.usuario = usuario;
        this.asiento = asiento;
    }

    public User getUsuario() {
        return usuario;
    }

    public int getAsiento() {
        return asiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        if (asiento != reserva.asiento) return false;
        return usuario != null ? usuario.equals(reserva.usuario) : reserva.usuario == null;
    }

    @Override
    public int hashCode() {
        int result = usuario != null ? usuario.hashCode() : 0;
        result = 31 * result + asiento;
        return result;
    }
}