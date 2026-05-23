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
  private volatile boolean modoSincronizado = true;

  public ReservaService(Vuelo vuelo) {
    this.vuelo = vuelo;
  }

  public boolean isModoSincronizado() {
    return modoSincronizado;
  }

  public void setModoSincronizado(boolean modoSincronizado) {
    this.modoSincronizado = modoSincronizado;
  }

  public String reservar(User usuario, int asiento) {
    return modoSincronizado
        ? reservarSincronizado(usuario, asiento)
        : reservarSinSincronizacion(usuario, asiento);
  }

  /**
   * Versión con exclusión mutua: sección crítica protegida por el monitor de
   * Vuelo.
   */
  private String reservarSincronizado(User usuario, int asiento) {
    synchronized (vuelo) {
      if (asiento < 1 || asiento > 30)
        return "El asiento debe estar entre 1 y 30.";

      Reserva existente = vuelo.getReserva(asiento);
      if (existente != null && existente.getUsuario().getCedula().equals(usuario.getCedula()))
        return "El cliente " + usuario.getNombre() + " ya tiene reservado este asiento.";

      Queue<Reserva> cola = vuelo.obtenerCola(asiento);
      boolean yaEnCola = cola.stream()
          .anyMatch(r -> r.getUsuario().getCedula().equals(usuario.getCedula()));
      if (yaEnCola)
        return "El cliente " + usuario.getNombre() + " ya esta en la cola de espera de este asiento.";

      if (vuelo.estaLibre(asiento)) {
        vuelo.ocupar(asiento, new Reserva(usuario, asiento));
        vuelo.notifyAll(); // despierta hilos en espera
        return null;
      } else {
        cola.offer(new Reserva(usuario, asiento));
        return "El asiento " + asiento + " ya esta ocupado. Se agrega la lista de espera.";
      }
    }
  }

  /**
   * Versión SIN exclusión mutua.
   */
  private String reservarSinSincronizacion(User usuario, int asiento) {
    if (asiento < 1 || asiento > 30)
      return "El asiento debe estar entre 1 y 30.";

    Reserva existente = vuelo.getReserva(asiento);
    if (existente != null && existente.getUsuario().getCedula().equals(usuario.getCedula()))
      return "El cliente " + usuario.getNombre() + " ya tiene reservado este asiento.";

    if (vuelo.estaLibre(asiento)) {
      vuelo.ocupar(asiento, new Reserva(usuario, asiento)); // lock adquirido y liberado
      return null;
    } else {
      Queue<Reserva> cola = vuelo.obtenerCola(asiento);
      cola.offer(new Reserva(usuario, asiento));
      return "El asiento " + asiento + " ya esta ocupado. Se agrega la lista de espera.";
    }
  }

  public Reserva eliminar(int asiento) {
    return modoSincronizado
        ? eliminarSincronizado(asiento)
        : eliminarSinSincronizacion(asiento);
  }

  private Reserva eliminarSincronizado(int asiento) {
    synchronized (vuelo) {
      if (asiento < 1 || asiento > 30)
        return null;
      Reserva reserva = vuelo.getReserva(asiento);
      if (reserva != null) {
        vuelo.liberar(asiento);
        Queue<Reserva> cola = vuelo.obtenerCola(asiento);
        if (!cola.isEmpty()) {
          vuelo.ocupar(asiento, cola.poll());
        }
        vuelo.notifyAll();
      }
      return reserva;
    }
  }

  private Reserva eliminarSinSincronizacion(int asiento) {
    if (asiento < 1 || asiento > 30)
      return null;
    Reserva reserva = vuelo.getReserva(asiento);
    if (reserva != null) {
      vuelo.liberar(asiento);
      Queue<Reserva> cola = vuelo.obtenerCola(asiento);
      if (!cola.isEmpty()) {
        vuelo.ocupar(asiento, cola.poll());
      }
    }
    return reserva;
  }

  public boolean cambiarAsiento(User usuario, int oldAsiento, int newAsiento) {
    return modoSincronizado
        ? cambiarAsientoSincronizado(usuario, oldAsiento, newAsiento)
        : cambiarAsientoSinSincronizacion(usuario, oldAsiento, newAsiento);
  }

  private boolean cambiarAsientoSincronizado(User usuario, int oldAsiento, int newAsiento) {
    synchronized (vuelo) {
      if (oldAsiento < 1 || oldAsiento > 30 || newAsiento < 1 || newAsiento > 30)
        return false;
      if (oldAsiento == newAsiento)
        return true;

      Reserva reservaActual = vuelo.getReserva(oldAsiento);
      if (reservaActual == null || !reservaActual.getUsuario().getCedula().equals(usuario.getCedula()))
        return false;

      vuelo.liberar(oldAsiento);
      if (vuelo.estaLibre(newAsiento)) {
        vuelo.ocupar(newAsiento, new Reserva(usuario, newAsiento));
        vuelo.notifyAll();
        return true;
      }
      vuelo.ocupar(oldAsiento, reservaActual); // rollback
      return false;
    }
  }

  private boolean cambiarAsientoSinSincronizacion(User usuario, int oldAsiento, int newAsiento) {
    if (oldAsiento < 1 || oldAsiento > 30 || newAsiento < 1 || newAsiento > 30)
      return false;
    if (oldAsiento == newAsiento)
      return true;

    Reserva reservaActual = vuelo.getReserva(oldAsiento);
    if (reservaActual == null || !reservaActual.getUsuario().getCedula().equals(usuario.getCedula()))
      return false;

    vuelo.liberar(oldAsiento);
    if (vuelo.estaLibre(newAsiento)) {
      vuelo.ocupar(newAsiento, new Reserva(usuario, newAsiento));
      return true;
    }
    vuelo.ocupar(oldAsiento, reservaActual);
    return false;
  }

  public int getAsientosDisponibles() {
    if (modoSincronizado) {
      synchronized (vuelo) {
        return vuelo.asientosDisponibles();
      }
    }
    return vuelo.asientosDisponibles();
  }

  public Map<Integer, String> getMapaAsientos() {
    if (modoSincronizado) {
      synchronized (vuelo) {
        return buildMapaAsientos();
      }
    }
    return buildMapaAsientos();
  }

  private Map<Integer, String> buildMapaAsientos() {
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

  public Map<Integer, List<String>> getListaEspera() {
    if (modoSincronizado) {
      synchronized (vuelo) {
        return buildListaEspera();
      }
    }
    return buildListaEspera();
  }

  private Map<Integer, List<String>> buildListaEspera() {
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

  public Vuelo getVuelo() {
    return vuelo;
  }
}
