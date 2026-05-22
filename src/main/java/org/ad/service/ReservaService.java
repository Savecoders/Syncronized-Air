package org.ad.service;

import org.ad.core.reserva.Reserva;
import org.ad.core.users.User;
import org.ad.core.vuelo.Vuelo;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class ReservaService {

  private final Vuelo vuelo;

  public ReservaService(Vuelo vuelo) {
    this.vuelo = vuelo;
  }

  public String reservar(User usuario, int asiento) {
    synchronized (vuelo) {
      if (asiento < 1 || asiento > 30)
        return "El asiento debe estar entre 1 y 30.";

      Reserva existente = vuelo.getReserva(asiento);
      if (existente != null && existente.getUsuario().getCedula().equals(usuario.getCedula())) {
        return "El cliente " + usuario.getNombre() + " ya tiene reservado este asiento.";
      }

      Queue<Reserva> cola = vuelo.obtenerCola(asiento);
      boolean yaEnCola = cola.stream()
          .anyMatch(r -> r.getUsuario().getCedula().equals(usuario.getCedula()));
      if (yaEnCola) {
        return "El cliente " + usuario.getNombre() + " ya esta en la cola de espera de este asiento.";
      }

      if (vuelo.estaLibre(asiento)) {
        vuelo.ocupar(asiento, new Reserva(usuario, asiento));
        vuelo.notifyAll(); // Wake up waiting threads!
        return null;
      } else {
        cola.offer(new Reserva(usuario, asiento));
        return "El asiento " + asiento + " ya esta ocupado. Se agrega la lista de espera.";
      }
    }
  }

  public Reserva eliminar(int asiento) {
    synchronized (vuelo) {
      if (asiento < 1 || asiento > 30)
        return null;
      Reserva reserva = vuelo.getReserva(asiento);
      if (reserva != null) {
        vuelo.liberar(asiento);
        Queue<Reserva> cola = vuelo.obtenerCola(asiento);
        if (!cola.isEmpty()) {
          Reserva siguiente = cola.poll();
          vuelo.ocupar(asiento, siguiente);
        }
        vuelo.notifyAll(); // Wake up waiting threads!
      }
      return reserva;
    }
  }

  public boolean cambiarAsiento(User usuario, int oldAsiento, int newAsiento) {
    synchronized (vuelo) {
      if (oldAsiento < 1 || oldAsiento > 30 || newAsiento < 1 || newAsiento > 30)
        return false;
      if (oldAsiento == newAsiento)
        return true;

      Reserva reservaActual = vuelo.getReserva(oldAsiento);
      if (reservaActual == null || !reservaActual.getUsuario().getCedula().equals(usuario.getCedula())) {
        return false;
      }

      vuelo.liberar(oldAsiento);

      if (vuelo.estaLibre(newAsiento)) {
        vuelo.ocupar(newAsiento, new Reserva(usuario, newAsiento));
        vuelo.notifyAll(); // Wake up waiting threads!
        return true;
      }

      vuelo.ocupar(oldAsiento, reservaActual);
      return false;
    }
  }

  public int getAsientosDisponibles() {
    synchronized (vuelo) {
      return vuelo.asientosDisponibles();
    }
  }

  public Map<Integer, String> getMapaAsientos() {
    synchronized (vuelo) {
      Map<Integer, String> mapa = new LinkedHashMap<>();
      for (int i = 1; i <= 30; i++) {
        if (vuelo.estaLibre(i)) {
          mapa.put(i, null);
        } else {
          Reserva r = vuelo.getReserva(i);
          mapa.put(i, r != null ? r.getUsuario().getNombre() : "Ocupado");
        }
      }
      return mapa;
    }
  }

  public Map<Integer, List<String>> getListaEspera() {
    synchronized (vuelo) {
      Map<Integer, List<String>> lista = new LinkedHashMap<>();
      for (Map.Entry<Integer, Queue<Reserva>> entry : vuelo.getColas().entrySet()) {
        Queue<Reserva> cola = entry.getValue();
        if (!cola.isEmpty()) {
          lista.put(entry.getKey(),
              cola.stream().map(r -> r.getUsuario().getNombre()).collect(Collectors.toList()));
        }
      }
      return lista;
    }
  }

  public Vuelo getVuelo() {
    return vuelo;
  }
}
