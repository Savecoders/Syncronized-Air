package org.ad.core.simulacion;

import org.ad.core.reserva.Reserva;
import org.ad.core.vuelo.Vuelo;
import org.ad.service.ReservaService;

import java.util.function.Consumer;

public class SimulacionReservaConcurrente extends Thread {

    private final Vuelo vuelo;
    private final ReservaService reservaService;
    private final Reserva reserva;
    private final Consumer<String> logCallback;

    public SimulacionReservaConcurrente(
            String nombre,
            Vuelo vuelo,
            ReservaService reservaService,
            Reserva reserva) {

        this(nombre, vuelo, reservaService, reserva, null);
    }

    public SimulacionReservaConcurrente(
            String nombre,
            Vuelo vuelo,
            ReservaService reservaService,
            Reserva reserva,
            Consumer<String> logCallback) {

        super(nombre);
        this.vuelo = vuelo;
        this.reservaService = reservaService;
        this.reserva = reserva;
        this.logCallback = logCallback;
    }

    private void log(String msg) {
        System.out.println(msg);
        if (logCallback != null) {
            logCallback.accept(msg);
        }
    }

    @Override
    public void run() {
        int asiento = reserva.getAsiento();

        try {
            log(getName() + " atendiendo a " + reserva.getUsuario().getNombre());
            Thread.sleep((int) (Math.random() * 2000) + 1000);

            synchronized (vuelo) {
                reservaService.reservar(reserva.getUsuario(), asiento);

                while (vuelo.getReserva(asiento) == null ||
                       !reserva.getUsuario().equals(vuelo.getReserva(asiento).getUsuario())) {

                    log(reserva.getUsuario().getNombre() + " espera asiento " + asiento);
                    vuelo.wait();
                }

                log(reserva.getUsuario().getNombre() + " reservó asiento " + asiento);
            }

            Thread.sleep(4000);

            synchronized (vuelo) {
                Reserva actual = vuelo.getReserva(asiento);
                if (actual != null && reserva.getUsuario().equals(actual.getUsuario())) {
                    reservaService.eliminar(asiento);
                    log(reserva.getUsuario().getNombre() + " liberó asiento " + asiento);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Hilo interrumpido: " + getName());
        }
    }
}
