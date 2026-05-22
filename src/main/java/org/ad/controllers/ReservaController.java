package org.ad.controllers;

import org.ad.core.reserva.Reserva;
import org.ad.core.simulacion.SimulacionReservaConcurrente;
import org.ad.core.users.User;
import org.ad.core.vuelo.Vuelo;
import org.ad.service.ReservaService;
import org.ad.service.UserService;
import org.ad.views.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;

public class ReservaController {

  private final Vuelo vuelo;
  private final UserService userService;
  private final ReservaService reservaService;
  private MainFrame mainFrame;

  public ReservaController(Vuelo vuelo, UserService userService, ReservaService reservaService) {
    this.vuelo = vuelo;
    this.userService = userService;
    this.reservaService = reservaService;

    vuelo.addListener(() -> SwingUtilities.invokeLater(this::refrescarVistas));
  }

  public void setMainFrame(MainFrame mainFrame) {
    this.mainFrame = mainFrame;
  }

  public void refrescarVistas() {
    if (mainFrame != null) {
      mainFrame.refrescarTodo();
    }
  }

  public String reservarAsiento(String cedula, int asiento) {
    if (asiento < 1 || asiento > 30)
      return "El asiento debe estar entre 1 y 30.";
    User user = userService.buscarPorCedula(cedula).orElse(null);
    if (user == null)
      return "Cliente no encontrado.";
    String resultado = reservaService.reservar(user, asiento);
    refrescarVistas();
    return resultado;
  }

  public String eliminarReservacion(int asiento) {
    if (asiento < 1 || asiento > 30)
      return "El asiento debe estar entre 1 y 30.";
    Reserva r = reservaService.eliminar(asiento);
    refrescarVistas();
    return r != null ? null : "No hay reservacion en el asiento " + asiento;
  }

  public String cambiarAsiento(String cedula, int oldAsiento, int newAsiento) {
    if (oldAsiento < 1 || oldAsiento > 30 || newAsiento < 1 || newAsiento > 30) {
      return "Los asientos deben estar entre 1 y 30.";
    }
    User user = userService.buscarPorCedula(cedula).orElse(null);
    if (user == null)
      return "Cliente no encontrado.";
    boolean exito = reservaService.cambiarAsiento(user, oldAsiento, newAsiento);
    refrescarVistas();
    return exito ? null
        : "No se pudo cambiar el asiento. El nuevo asiento esta ocupado o la reserva original no existe.";
  }

  public void simularConcurrencia() {
    List<User> users = userService.getClientes();
    if (users.size() < 3) {
      JOptionPane.showMessageDialog(mainFrame,
          "Se necesitan al menos 3 clientes registrados para simular concurrencia.",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    int asientoObj = 5;
    for (int i = 0; i < 3; i++) {
      User u = users.get(i);
      Reserva r = new Reserva(u, asientoObj);
      final int idx = i;
      SimulacionReservaConcurrente sim = new SimulacionReservaConcurrente(
          "Ventanilla " + (i + 1),
          vuelo,
          reservaService,
          r,
          msg -> SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
              mainFrame.appendLog(idx, msg);
              mainFrame.refrescarTodo();
            }
          }));
      sim.start();
    }
  }

  public int getAsientosDisponibles() {
    return reservaService.getAsientosDisponibles();
  }

  public Map<Integer, String> getMapaAsientos() {
    return reservaService.getMapaAsientos();
  }

  public Map<Integer, List<String>> getListaEspera() {
    return reservaService.getListaEspera();
  }
}
