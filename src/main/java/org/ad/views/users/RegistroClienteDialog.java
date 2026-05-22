package org.ad.views.users;

import org.ad.controllers.UserController;

import javax.swing.*;
import java.awt.*;

public class RegistroClienteDialog extends JDialog {

  private final JTextField cedulaField = new JTextField(15);
  private final JTextField nombreField = new JTextField(15);
  private final JLabel errorLabel = new JLabel(" ");
  private final UserController userController;

  public RegistroClienteDialog(JFrame parent, UserController userController) {
    super(parent, "Registrar Cliente", true);
    this.userController = userController;
    getContentPane().setBackground(Color.WHITE);
    setLayout(new BorderLayout(10, 10));

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(5, 10, 5, 10);

    gbc.gridx = 0;
    gbc.gridy = 0;
    form.add(new JLabel("Cedula (10 digitos):"), gbc);
    gbc.gridx = 1;
    cedulaField.setPreferredSize(new Dimension(200, 25));
    form.add(cedulaField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    form.add(new JLabel("Nombre:"), gbc);
    gbc.gridx = 1;
    nombreField.setPreferredSize(new Dimension(200, 25));
    form.add(nombreField, gbc);

    JPanel centerWrap = new JPanel(new BorderLayout());
    centerWrap.setBackground(Color.WHITE);
    centerWrap.add(form, BorderLayout.CENTER);
    add(centerWrap, BorderLayout.CENTER);

    errorLabel.setForeground(Color.RED);
    errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    errorPanel.setBackground(Color.WHITE);
    errorPanel.add(errorLabel);
    add(errorPanel, BorderLayout.NORTH);

    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
    btnPanel.setBackground(Color.WHITE);
    JButton guardarBtn = new JButton("Guardar");
    guardarBtn.addActionListener(e -> guardar());
    JButton cancelarBtn = new JButton("Cancelar");
    cancelarBtn.addActionListener(e -> dispose());
    btnPanel.add(guardarBtn);
    btnPanel.add(cancelarBtn);
    add(btnPanel, BorderLayout.SOUTH);

    pack();
    setLocationRelativeTo(parent);
  }

  private void guardar() {
    String cedula = cedulaField.getText().trim();
    String nombre = nombreField.getText().trim();

    String error = userController.registrarCliente(cedula, nombre);
    if (error != null) {
      errorLabel.setText(error);
      return;
    }
    dispose();
  }
}
