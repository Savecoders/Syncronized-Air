package org.ad.views.reserva;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class WaitListPanel extends JPanel {

  private final JPanel contentPanel;

  public WaitListPanel() {
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder("Lista de Espera"));

    contentPanel = new JPanel();
    contentPanel.setBackground(Color.WHITE);
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

    JScrollPane scroll = new JScrollPane(contentPanel);
    scroll.getViewport().setBackground(Color.WHITE);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    add(scroll, BorderLayout.CENTER);
  }

  public void actualizar(Map<Integer, List<String>> listaEspera) {
    contentPanel.removeAll();
    if (listaEspera == null || listaEspera.isEmpty()) {
      JLabel empty = new JLabel("No hay clientes en espera.");
      empty.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      contentPanel.add(empty);
    } else {
      for (Map.Entry<Integer, List<String>> entry : listaEspera.entrySet()) {
        JPanel seatPanel = new JPanel();
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JLabel header = new JLabel("Asiento " + entry.getKey());
        header.setFont(new Font("Arial", Font.BOLD, 15));
        seatPanel.add(header);

        for (String nombre : entry.getValue()) {
          JLabel nameLabel = new JLabel(" * " + nombre);
          nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
          seatPanel.add(nameLabel);
        }

        contentPanel.add(seatPanel);
      }
    }
    contentPanel.revalidate();
    contentPanel.repaint();
  }
}
