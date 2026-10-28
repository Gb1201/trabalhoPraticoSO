package trabalhoPraticoSO.Codigos.tresrobos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


public class Main {

    static final int CICLOS = 4;

    static final Semaphore semBart = new Semaphore(1);
    static final Semaphore semLisa1 = new Semaphore(0);
    static final Semaphore semMaggie = new Semaphore(0);
    static final Semaphore semLisa2 = new Semaphore(0);

    static final List<String> historico = new ArrayList<>();   // acessado sempre por um robo por vez

    static void move(String robo) {
        historico.add(robo);
        System.out.println("  move(): " + robo);
    }

    static class Bart extends Thread {
        public void run() {
            try {
                for (int i = 0; i < CICLOS; i++) {
                    semBart.acquire();
                    move("Bart");
                    semLisa1.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    static class Lisa extends Thread {
        public void run() {
            try {
                for (int i = 0; i < CICLOS; i++) {
                    semLisa1.acquire();
                    move("Lisa");
                    semMaggie.release();

                    semLisa2.acquire();
                    move("Lisa");
                    semBart.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    static class Maggie extends Thread {
        public void run() {
            try {
                for (int i = 0; i < CICLOS; i++) {
                    semMaggie.acquire();
                    move("Maggie");
                    semLisa2.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        historico.clear();
        semBart.drainPermits(); semBart.release();
        semLisa1.drainPermits(); semMaggie.drainPermits(); semLisa2.drainPermits();

        System.out.println("=== TRES ROBOS (" + CICLOS + " ciclos) ===");
        Thread[] ts = {new Bart(), new Lisa(), new Maggie()};
        // inicia fora de ordem de proposito: o semaforo, nao a ordem de start, define a sequencia
        ts[2].start(); ts[1].start(); ts[0].start();
        for (Thread t : ts) t.join();

        String[] padrao = {"Bart", "Lisa", "Maggie", "Lisa"};
        boolean ok = historico.size() == CICLOS * 4;
        for (int i = 0; ok && i < historico.size(); i++) ok = historico.get(i).equals(padrao[i % 4]);

        System.out.println("--- VALIDACAO ---");
        System.out.println("Sequencia: " + historico);
        System.out.println(ok ? "OK: sequencia Bart->Lisa->Maggie->Lisa respeitada em todos os ciclos."
                              : "ERRO: sequencia fora de ordem!");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
