package org.ad.views.vuelos;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AsientosMapPanel extends JPanel {

  private final JPanel[] seatCells = new JPanel[30];
  private final JLabel[] occupantLabels = new JLabel[30];

  public AsientosMapPanel() {
    setLayout(new GridLayout(5, 6, 5, 5));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder("Mapa de Asientos"));

    for (int i = 0; i < 30; i++) {
      int seatNum = i + 1;
      JPanel cell = new JPanel(new BorderLayout());
      cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

      JLabel numLabel = new JLabel(String.valueOf(seatNum), SwingConstants.CENTER);
      numLabel.setFont(new Font("Arial", Font.BOLD, 14));

      JLabel occLabel = new JLabel("", SwingConstants.CENTER);
      occLabel.setFont(new Font("Arial", Font.PLAIN, 10));

      cell.add(numLabel, BorderLayout.NORTH);
      cell.add(occLabel, BorderLayout.CENTER);
      cell.setBackground(Color.WHITE);

      seatCells[i] = cell;
      occupantLabels[i] = occLabel;
      add(cell);
    }
  }

  public void actualizar(Map<Integer, String> mapaAsientos) {
    for (int i = 0; i < 30; i++) {
      int seatNum = i + 1;
      String ocupante = mapaAsientos.get(seatNum);
      if (ocupante == null) {
        seatCells[i].setBackground(Color.WHITE);
        occupantLabels[i].setText("");
      } else {
        seatCells[i].setBackground(new Color(144, 238, 144));
        occupantLabels[i].setText(ocupante);
      }
    }
  }
}
