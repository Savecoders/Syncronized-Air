package org.ad.core.vuelo;

import org.ad.core.reserva.Reserva;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Vuelo {

    private final boolean[] asientos = new boolean[30];
    private final Reserva[] reservasActivas = new Reserva[30];
    private final Map<Integer, Queue<Reserva>> colas = new HashMap<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public synchronized boolean estaLibre(int asiento) {
        return !asientos[asiento - 1];
    }

    public synchronized void ocupar(int asiento, Reserva reserva) {
        asientos[asiento - 1] = true;
        reservasActivas[asiento - 1] = reserva;
        notificar();
    }

    public synchronized void ocupar(int asiento) {
        asientos[asiento - 1] = true;
        notificar();
    }

    public synchronized void liberar(int asiento) {
        asientos[asiento - 1] = false;
        reservasActivas[asiento - 1] = null;
        notificar();
    }

    public synchronized Reserva getReserva(int asiento) {
        return reservasActivas[asiento - 1];
    }

    public synchronized Queue<Reserva> obtenerCola(int asiento) {
        return colas.computeIfAbsent(asiento, k -> new LinkedList<>());
    }

    public synchronized int asientosDisponibles() {
        int count = 0;
        for (boolean b : asientos) {
            if (!b) count++;
        }
        return count;
    }

    public synchronized Map<Integer, Queue<Reserva>> getColas() {
        Map<Integer, Queue<Reserva>> copy = new HashMap<>();
        for (Map.Entry<Integer, Queue<Reserva>> entry : colas.entrySet()) {
            copy.put(entry.getKey(), new LinkedList<>(entry.getValue()));
        }
        return copy;
    }

    public synchronized void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void notificar() {
        List<Runnable> copy;
        synchronized (this) {
            copy = new ArrayList<>(listeners);
        }
        for (Runnable r : copy) {
            r.run();
        }
    }
}
