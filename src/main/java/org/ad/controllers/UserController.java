package org.ad.controllers;

import org.ad.core.users.User;
import org.ad.service.UserService;
import org.ad.views.MainFrame;

import java.util.List;

public class UserController {

  private final UserService userService;
  private MainFrame mainFrame;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  public void setMainFrame(MainFrame mainFrame) {
    this.mainFrame = mainFrame;
  }

  private void refrescarVistas() {
    if (mainFrame != null) {
      mainFrame.refrescarTodo();
    }
  }

  public List<User> getClientes() {
    return userService.getClientes();
  }

  public String registrarCliente(String cedula, String nombre) {
    String error = userService.registrarCliente(cedula, nombre);
    if (error == null) {
      refrescarVistas();
    }
    return error;
  }
}
