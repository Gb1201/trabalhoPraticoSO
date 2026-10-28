package trabalhoPraticoSO.Codigos.fumantes;

import java.util.Random;
import java.util.concurrent.Semaphore;


public class Main {

    static final String[] NOMES = {"Papel", "Tabaco", "Fosforo"};
    static final int RODADAS = 9;

    static final Semaphore agente = new Semaphore(1);
    static final Semaphore[] fumante = {new Semaphore(0), new Semaphore(0), new Semaphore(0)};
    static final Random rnd = new Random();

    static volatile boolean fim = false;
    static volatile int mesaA = -1, mesaB = -1;      // ingredientes na mesa (-1 = vazia)
    static final int[] cigarros = new int[3];
    static volatile boolean violacao = false;

    static class Representante extends Thread {
        Representante() { super("Representante"); }
        public void run() {
            try {
                for (int r = 1; r <= RODADAS; r++) {
                    agente.acquire();
                    int falta = rnd.nextInt(3);          // ingrediente que o fumante escolhido possui
                    mesaA = (falta + 1) % 3;
                    mesaB = (falta + 2) % 3;
                    System.out.println("[Representante] Rodada " + r + ": coloca " + NOMES[mesaA]
                            + " e " + NOMES[mesaB] + " na mesa -> acorda fumante com " + NOMES[falta]);
                    fumante[falta].release();
                }
                agente.acquire();                        // espera o ultimo fumante terminar
                fim = true;
                for (Semaphore s : fumante) s.release(); // libera todos para encerrar
            } catch (InterruptedException e) { }
        }
    }

    static class Fumante extends Thread {
        final int meu;
        Fumante(int meu) { super("Fumante-" + NOMES[meu]); this.meu = meu; }
        public void run() {
            try {
                while (true) {
                    fumante[meu].acquire();              // bloqueado ate ser chamado
                    if (fim) return;
                    // valida: a mesa deve ter exatamente os dois ingredientes que NAO sao meus
                    if (mesaA == meu || mesaB == meu || mesaA == -1) violacao = true;
                    System.out.println("   [Fumante " + NOMES[meu] + "] pega " + NOMES[mesaA] + " e "
                            + NOMES[mesaB] + ", enrola e fuma...");
                    mesaA = mesaB = -1;
                    Thread.sleep(100 + rnd.nextInt(300));
                    cigarros[meu]++;
                    System.out.println("   [Fumante " + NOMES[meu] + "] terminou de fumar.");
                    agente.release();                    // avisa o representante
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        agente.drainPermits(); agente.release();
        for (Semaphore s : fumante) s.drainPermits();
        fim = false; violacao = false; mesaA = mesaB = -1;
        java.util.Arrays.fill(cigarros, 0);

        System.out.println("=== FUMANTES (" + RODADAS + " rodadas) ===");
        Thread[] fs = {new Fumante(0), new Fumante(1), new Fumante(2)};
        Representante rep = new Representante();
        for (Thread t : fs) t.start();
        rep.start();
        rep.join();
        for (Thread t : fs) t.join();

        int total = cigarros[0] + cigarros[1] + cigarros[2];
        System.out.println("--- VALIDACAO ---");
        System.out.println("Cigarros: Papel=" + cigarros[0] + " Tabaco=" + cigarros[1]
                + " Fosforo=" + cigarros[2] + " | Total=" + total + " / " + RODADAS);
        System.out.println(total == RODADAS && !violacao
                ? "OK: cada rodada gerou exatamente um cigarro, sem deadlock e com o fumante correto."
                : "ERRO: inconsistencia detectada!");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
