package org.ad.views.reserva;

import org.ad.core.users.User;
import org.ad.controllers.ReservaController;
import org.ad.controllers.UserController;

import javax.swing.*;
import java.awt.*;

public class VentanillaPanel extends JPanel {

  private final JComboBox<User> clienteCombo;
  private final JSpinner asientoSpinner;
  private final JTextPane logArea;
  private final java.util.List<String> logEntries = new java.util.ArrayList<>();
  private final ReservaController reservaController;
  private final UserController userController;
  private final int index;

  public VentanillaPanel(String titulo, ReservaController reservaController, UserController userController, int index) {
    this.reservaController = reservaController;
    this.userController = userController;
    this.index = index;
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
        titulo,
        javax.swing.border.TitledBorder.LEFT,
        javax.swing.border.TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 12),
        new Color(71, 85, 105) // Slate-600
    ));
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

    logArea = new JTextPane();
    logArea.setContentType("text/html");
    logArea.setEditable(false);
    logArea.setBackground(new Color(13, 12, 12));
    logArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JScrollPane scroll = new JScrollPane(logArea);
    scroll.getViewport().setBackground(new Color(13, 12, 12));
    scroll.setBorder(BorderFactory.createLineBorder(new Color(13, 12, 12), 1));
    scroll.setPreferredSize(new Dimension(200, 120));
    add(scroll, BorderLayout.SOUTH);

    updateLogView();
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

    String color = "#e2e8f0";
    String msgLower = msg.toLowerCase();

    if (msgLower.contains("error") || msgLower.contains("invalido") || msgLower.contains("seleccione")
        || msgLower.contains("fallo")) {
      color = "#f87171"; // soft red
    } else if (msgLower.contains("reservado") || msgLower.contains("eliminada") || msgLower.contains("cambiado")
        || msgLower.contains("exito")) {
      color = "#34d399"; // soft green
    } else if (msgLower.contains("espera") || msgLower.contains("cola") || msgLower.contains("esperando")) {
      color = "#fbbf24"; // soft amber/yellow
    }

    String escapedMsg = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    String formattedLine = String.format(
        "<div style='margin-bottom: 2px;'><span style='color: #64748b; font-weight: bold;'>[%s]</span> <span style='color: %s;'>%s</span></div>",
        timestamp, color, escapedMsg);

    logEntries.add(formattedLine);
    if (logEntries.size() > 100) {
      logEntries.remove(0);
    }

    updateLogView();
  }

  private void updateLogView() {
    StringBuilder html = new StringBuilder();
    html.append(
        "<html><body style='font-family: Consolas, \"Courier New\", Monospaced; font-size: 10px; background-color: #010101; margin: 3px; padding: 0;'>");
    for (String entry : logEntries) {
      html.append(entry);
    }
    html.append("</body></html>");
    logArea.setText(html.toString());

    // Auto scroll to bottom
    SwingUtilities.invokeLater(() -> {
      logArea.setCaretPosition(logArea.getDocument().getLength());
    });
  }

  public void clearLog() {
    logEntries.clear();
    updateLogView();
  }
}
