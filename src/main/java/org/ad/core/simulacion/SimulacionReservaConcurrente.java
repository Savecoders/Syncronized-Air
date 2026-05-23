package org.ad.core.simulacion;

import org.ad.core.reserva.Reserva;
import org.ad.core.vuelo.Vuelo;
import org.ad.service.ReservaService;

import java.util.function.Consumer;

public class SimulacionReservaConcurrente extends Thread {

  private final Vuelo vuelo;
  private final ReservaService reservaService;
  private final Reserva reserva;
  private final Consumer<String> logCallback;

  public SimulacionReservaConcurrente(
      String nombre,
      Vuelo vuelo,
      ReservaService reservaService,
      Reserva reserva) {
    this(nombre, vuelo, reservaService, reserva, null);
  }

  public SimulacionReservaConcurrente(
      String nombre,
      Vuelo vuelo,
      ReservaService reservaService,
      Reserva reserva,
      Consumer<String> logCallback) {
    super(nombre);
    this.vuelo = vuelo;
    this.reservaService = reservaService;
    this.reserva = reserva;
    this.logCallback = logCallback;
  }

  private void log(String msg) {
    System.out.println(msg);
    if (logCallback != null)
      logCallback.accept(msg);
  }

  @Override
  public void run() {
    if (reservaService.isModoSincronizado()) {
      runSincronizado();
    } else {
      runSinSincronizacion();
    }
  }

  private void runSincronizado() {
    int asiento = reserva.getAsiento();
    try {
      log("[SYNC] " + getName() + " atendiendo a " + reserva.getUsuario().getNombre());
      Thread.sleep((int) (Math.random() * 2000) + 1000);

      synchronized (vuelo) {
        reservaService.reservar(reserva.getUsuario(), asiento);

        // Espera activa con wait() hasta que este usuario tenga el asiento
        while (vuelo.getReserva(asiento) == null ||
            !reserva.getUsuario().equals(vuelo.getReserva(asiento).getUsuario())) {
          log("[SYNC] " + reserva.getUsuario().getNombre() + " espera asiento " + asiento);
          vuelo.wait(); // libera el lock y duerme hasta notifyAll()
        }
        log("[SYNC] " + reserva.getUsuario().getNombre() + " reservó asiento " + asiento);
      }

      Thread.sleep(4000);

      synchronized (vuelo) {
        Reserva actual = vuelo.getReserva(asiento);
        if (actual != null && reserva.getUsuario().equals(actual.getUsuario())) {
          reservaService.eliminar(asiento);
          log("[SYNC] " + reserva.getUsuario().getNombre() + " liberó asiento " + asiento);
        }
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log("Hilo interrumpido: " + getName());
    }
  }

  private void runSinSincronizacion() {
    int asiento = reserva.getAsiento();
    try {
      log("[NO-SYNC] " + getName() + " atendiendo a " + reserva.getUsuario().getNombre());
      Thread.sleep((int) (Math.random() * 2000) + 1000);

      // Sin bloque synchronized — la reserva NO es atómica
      String resultado = reservaService.reservar(reserva.getUsuario(), asiento);

      if (resultado == null) {
        log("[NO-SYNC] " + reserva.getUsuario().getNombre()
            + " reservó asiento " + asiento
            + " (verificar si hubo sobreescritura)");
      } else {
        log("[NO-SYNC] " + reserva.getUsuario().getNombre() + ": " + resultado);
      }

      Thread.sleep(4000);

      // Verificación y liberación tampoco son atómicas
      Reserva actual = vuelo.getReserva(asiento);
      if (actual != null && reserva.getUsuario().equals(actual.getUsuario())) {
        reservaService.eliminar(asiento);
        log("[NO-SYNC] " + reserva.getUsuario().getNombre() + " liberó asiento " + asiento);
      } else {
        log("[NO-SYNC] " + reserva.getUsuario().getNombre()
            + " NO pudo liberar asiento " + asiento
            + " — asiento tiene: "
            + (actual != null ? actual.getUsuario().getNombre() : "nadie")
            + " (condición de carrera detectada)");
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log("Hilo interrumpido: " + getName());
    }
  }
}
