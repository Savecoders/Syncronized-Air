package org.ad.views.users;

import org.ad.core.users.User;
import org.ad.controllers.UserController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReporteClientesDialog extends JDialog {

  private final UserController userController;

  public ReporteClientesDialog(JFrame parent, UserController userController) {
    super(parent, "Reporte de Clientes", true);
    this.userController = userController;
    getContentPane().setBackground(Color.WHITE);
    setLayout(new BorderLayout(10, 10));

    DefaultTableModel model = new DefaultTableModel(new String[] { "Cedula", "Nombre" }, 0) {
      @Override
      public boolean isCellEditable(int row, int col) {
        return false;
      }
    };
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.getColumnModel().getColumn(0).setPreferredWidth(120);
    table.getColumnModel().getColumn(1).setPreferredWidth(200);

    cargarClientes(model);

    JScrollPane tableScroll = new JScrollPane(table);
    tableScroll.getViewport().setBackground(Color.WHITE);
    add(tableScroll, BorderLayout.CENTER);

    JButton cerrarBtn = new JButton("Cerrar");
    cerrarBtn.addActionListener(e -> dispose());
    JPanel btnPanel = new JPanel(new FlowLayout());
    btnPanel.setBackground(Color.WHITE);
    btnPanel.add(cerrarBtn);
    add(btnPanel, BorderLayout.SOUTH);

    setSize(450, 350);
    setLocationRelativeTo(parent);
  }

  private void cargarClientes(DefaultTableModel model) {
    List<User> clientes = userController.getClientes();
    for (User u : clientes) {
      model.addRow(new Object[] { u.getCedula(), u.getNombre() });
    }
  }
}
