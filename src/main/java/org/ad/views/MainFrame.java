package org.ad.views;

import org.ad.controllers.ReservaController;
import org.ad.controllers.UserController;
import org.ad.views.reserva.VentanillaPanel;
import org.ad.views.reserva.WaitListPanel;
import org.ad.views.users.RegistroClienteDialog;
import org.ad.views.users.ReporteClientesDialog;
import org.ad.views.vuelos.AsientosMapPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

  private final ReservaController reservaController;
  private final UserController userController;
  private final VentanillaPanel[] ventanillas = new VentanillaPanel[3];
  private final AsientosMapPanel asientosMapPanel;
  private final WaitListPanel waitListPanel;

  public MainFrame(ReservaController reservaController, UserController userController) {
    super("Sistema de Reserva de Asientos - Vuelo AD");
    this.reservaController = reservaController;
    this.userController = userController;
    this.asientosMapPanel = new AsientosMapPanel();
    this.waitListPanel = new WaitListPanel();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    getContentPane().setBackground(Color.WHITE);
    setLayout(new BorderLayout());

    initTopPanel();
    initCenterPanel();
    initBottomPanel();

    setSize(1100, 750);
    setLocationRelativeTo(null);
  }

  private void initTopPanel() {
    JPanel top = new JPanel(new GridLayout(1, 3, 10, 0));
    top.setBackground(Color.WHITE);
    top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    for (int i = 0; i < 3; i++) {
      ventanillas[i] = new VentanillaPanel("Ventanilla " + (i + 1), reservaController, userController, i);
      top.add(ventanillas[i]);
    }
    add(top, BorderLayout.NORTH);
  }

  private void initCenterPanel() {
    JPanel center = new JPanel(new BorderLayout(10, 10));
    center.setBackground(Color.WHITE);

    JButton simularBtn = new JButton("SIMULAR CONCURRENCIA");
    simularBtn.setFont(new Font("Arial", Font.BOLD, 14));
    simularBtn.addActionListener(e -> reservaController.simularConcurrencia());
    JPanel simPanel = new JPanel(new FlowLayout());
    simPanel.setBackground(Color.WHITE);
    simPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    simPanel.add(simularBtn);
    center.add(simPanel, BorderLayout.NORTH);

    JScrollPane seatScroll = new JScrollPane(asientosMapPanel);
    seatScroll.getViewport().setBackground(Color.WHITE);
    JScrollPane waitScroll = new JScrollPane(waitListPanel);
    waitScroll.getViewport().setBackground(Color.WHITE);
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        seatScroll, waitScroll);
    split.setBackground(Color.WHITE);
    split.setResizeWeight(0.6);
    split.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    center.add(split, BorderLayout.CENTER);

    add(center, BorderLayout.CENTER);
  }

  private void initBottomPanel() {
    JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    bottom.setBackground(Color.WHITE);
    bottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    JButton registrarBtn = new JButton("Registrar Cliente");
    registrarBtn.setFont(new Font("Arial", Font.PLAIN, 13));
    registrarBtn.addActionListener(e -> {
      RegistroClienteDialog dialog = new RegistroClienteDialog(this, userController);
      dialog.setVisible(true);
    });

    JButton reporteBtn = new JButton("Reporte de Clientes");
    reporteBtn.setFont(new Font("Arial", Font.PLAIN, 13));
    reporteBtn.addActionListener(e -> {
      ReporteClientesDialog dialog = new ReporteClientesDialog(this, userController);
      dialog.setVisible(true);
    });

    bottom.add(registrarBtn);
    bottom.add(reporteBtn);
    add(bottom, BorderLayout.SOUTH);
  }

  public void refrescarTodo() {
    asientosMapPanel.actualizar(reservaController.getMapaAsientos());
    waitListPanel.actualizar(reservaController.getListaEspera());
    for (VentanillaPanel v : ventanillas) {
      v.refrescarClientes();
    }
  }

  public void appendLog(int ventanillaIdx, String msg) {
    if (ventanillaIdx >= 0 && ventanillaIdx < ventanillas.length) {
      ventanillas[ventanillaIdx].appendLog(msg);
    }
  }
}
