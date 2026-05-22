package org.ad.views.reserva;

import org.ad.core.users.User;
import org.ad.controllers.ReservaController;
import org.ad.controllers.UserController;

import javax.swing.*;
import java.awt.*;

public class VentanillaPanel extends JPanel {

  private final JComboBox<User> clienteCombo;
  private final JSpinner asientoSpinner;
  private final JTextArea logArea;
  private final ReservaController reservaController;
  private final UserController userController;
  private final int index;

  public VentanillaPanel(String titulo, ReservaController reservaController, UserController userController, int index) {
    this.reservaController = reservaController;
    this.userController = userController;
    this.index = index;
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder(titulo));
    setLayout(new BorderLayout(5, 5));

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(2, 5, 2, 5);

    gbc.gridx = 0;
    gbc.gridy = 0;
    form.add(new JLabel("Cliente:"), gbc);
    gbc.gridx = 1;
    clienteCombo = new JComboBox<>();
    clienteCombo.setPreferredSize(new Dimension(150, 25));
    form.add(clienteCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    form.add(new JLabel("Asiento:"), gbc);
    gbc.gridx = 1;
    asientoSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
    asientoSpinner.setPreferredSize(new Dimension(80, 25));
    form.add(asientoSpinner, gbc);

    add(form, BorderLayout.NORTH);

    JPanel btnPanel = new JPanel(new GridLayout(1, 3, 3, 0));
    btnPanel.setBackground(Color.WHITE);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JButton reservarBtn = new JButton("Reservar");
    reservarBtn.addActionListener(e -> reservar());
    JButton eliminarBtn = new JButton("Eliminar");
    eliminarBtn.addActionListener(e -> eliminar());
    JButton cambiarBtn = new JButton("Cambiar");
    cambiarBtn.addActionListener(e -> cambiar());
    btnPanel.add(reservarBtn);
    btnPanel.add(eliminarBtn);
    btnPanel.add(cambiarBtn);
    add(btnPanel, BorderLayout.CENTER);

    logArea = new JTextArea(6, 15);
    logArea.setEditable(false);
    logArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
    JScrollPane scroll = new JScrollPane(logArea);
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.setPreferredSize(new Dimension(200, 100));
    add(scroll, BorderLayout.SOUTH);
  }

  public void refrescarClientes() {
    User selected = (User) clienteCombo.getSelectedItem();
    String cedula = selected != null ? selected.getCedula() : null;
    clienteCombo.removeAllItems();
    for (User u : userController.getClientes()) {
      clienteCombo.addItem(u);
      if (cedula != null && u.getCedula().equals(cedula)) {
        clienteCombo.setSelectedItem(u);
      }
    }
  }

  private User getSelectedUser() {
    return (User) clienteCombo.getSelectedItem();
  }

  private int getAsiento() {
    return (int) asientoSpinner.getValue();
  }

  private void reservar() {
    User u = getSelectedUser();
    if (u == null) {
      appendLog("Seleccione un cliente.");
      return;
    }
    String resultado = reservaController.reservarAsiento(u.getCedula(), getAsiento());
    if (resultado == null) {
      appendLog("Asiento " + getAsiento() + " reservado para " + u.getNombre());
    } else if (resultado.startsWith("ESPERA:")) {
      appendLog(resultado.substring(7));
    } else if (resultado.startsWith("ERROR:")) {
      appendLog("Error: " + resultado.substring(6));
    } else {
      appendLog("Error: " + resultado);
    }
  }

  private void eliminar() {
    int asiento = getAsiento();
    String resultado = reservaController.eliminarReservacion(asiento);
    if (resultado == null) {
      appendLog("Reservacion del asiento " + asiento + " eliminada.");
    } else if (resultado.startsWith("ERROR:")) {
      appendLog("Error: " + resultado.substring(6));
    } else {
      appendLog("Error: " + resultado);
    }
  }

  private void cambiar() {
    User u = getSelectedUser();
    if (u == null) {
      appendLog("Seleccione un cliente.");
      return;
    }
    String input = JOptionPane.showInputDialog(this, "Nuevo asiento (1-30):", "Cambiar Asiento",
        JOptionPane.PLAIN_MESSAGE);
    if (input == null)
      return;
    try {
      int newAsiento = Integer.parseInt(input.trim());
      int oldAsiento = getAsiento();
      String resultado = reservaController.cambiarAsiento(u.getCedula(), oldAsiento, newAsiento);
      if (resultado == null) {
        appendLog("Asiento cambiado de " + oldAsiento + " a " + newAsiento);
      } else if (resultado.startsWith("ERROR:")) {
        appendLog("Error: " + resultado.substring(6));
      } else {
        appendLog("Error: " + resultado);
      }
    } catch (NumberFormatException e) {
      appendLog("Numero de asiento invalido.");
    }
  }

  public void appendLog(String msg) {
    String timestamp = java.time.LocalTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    logArea.append("[" + timestamp + "] " + msg + "\n");
    logArea.setCaretPosition(logArea.getDocument().getLength());
  }
}
