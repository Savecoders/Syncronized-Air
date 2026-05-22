package org.ad.views;

import org.ad.core.users.User;
import org.ad.core.vuelo.Vuelo;
import org.ad.controllers.ReservaController;
import org.ad.controllers.UserController;
import org.ad.repositories.users.UserRepository;
import org.ad.service.ReservaService;
import org.ad.service.UserService;
import org.ad.repositories.users.UserMemoryRepository;

import javax.swing.SwingUtilities;

public class MainApp {

  public static void launch(String[] args) {
    SwingUtilities.invokeLater(() -> {
      Vuelo vuelo = new Vuelo();
      UserRepository userRepo = new UserMemoryRepository();

      seedUsers(userRepo);

      UserService userService = new UserService(userRepo);
      ReservaService reservaService = new ReservaService(vuelo);
      ReservaController reservaController = new ReservaController(vuelo, userService, reservaService);
      UserController userController = new UserController(userService);
      MainFrame frame = new MainFrame(reservaController, userController);
      reservaController.setMainFrame(frame);
      userController.setMainFrame(frame);
      reservaController.refrescarVistas();
      frame.setVisible(true);
    });
  }

  private static void seedUsers(UserRepository userRepo) {
    userRepo.save(new User("1234567890", "Carlos"));
    userRepo.save(new User("2345678901", "Ana"));
    userRepo.save(new User("3456789012", "Luis"));
    userRepo.save(new User("4567890123", "Maria"));
  }
}
