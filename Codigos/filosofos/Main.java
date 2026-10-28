package trabalhoPraticoSO.Codigos.filosofos;

import java.util.concurrent.Semaphore;


public class Main {

    static final int N = 5;
    static final int REFEICOES = 3;

    static final Semaphore[] hashi = new Semaphore[N];
    static final Semaphore mesa = new Semaphore(N - 1);
    static final Semaphore mtxEstado = new Semaphore(1);
    static final boolean[] comendo = new boolean[N];
    static final int[] refeicoes = new int[N];
    static volatile boolean violacao = false;

    static class Filosofo extends Thread {
        final int id;
        Filosofo(int id) { super("Filosofo-" + id); this.id = id; }

        public void run() {
            try {
                for (int k = 1; k <= REFEICOES; k++) {
                    System.out.println("[Filosofo " + id + "] meditando...");
                    Thread.sleep((int) (Math.random() * 300));

                    System.out.println("[Filosofo " + id + "] com fome.");
                    mesa.acquire();                           // limita a N-1 na disputa
                    hashi[id].acquire();
                    hashi[(id + 1) % N].acquire();

                    mtxEstado.acquire();
                    if (comendo[(id + 1) % N] || comendo[(id + N - 1) % N]) violacao = true;
                    comendo[id] = true;
                    mtxEstado.release();

                    System.out.println("[Filosofo " + id + "] COMENDO (refeicao " + k + ")");
                    Thread.sleep(150 + (int) (Math.random() * 200));

                    mtxEstado.acquire();
                    comendo[id] = false; refeicoes[id]++;
                    mtxEstado.release();

                    hashi[id].release();
                    hashi[(id + 1) % N].release();
                    mesa.release();
                    System.out.println("[Filosofo " + id + "] terminou e devolveu os hashis.");
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        for (int i = 0; i < N; i++) { hashi[i] = new Semaphore(1); comendo[i] = false; refeicoes[i] = 0; }
        violacao = false;

        System.out.println("=== FILOSOFOS (" + N + " filosofos, " + REFEICOES + " refeicoes cada) ===");
        Filosofo[] fs = new Filosofo[N];
        for (int i = 0; i < N; i++) { fs[i] = new Filosofo(i); fs[i].start(); }
        for (Filosofo f : fs) f.join();

        int total = 0;
        for (int r : refeicoes) total += r;
        System.out.println("--- VALIDACAO ---");
        System.out.println("Refeicoes: " + java.util.Arrays.toString(refeicoes) + " | Total=" + total
                + " / " + (N * REFEICOES));
        System.out.println(!violacao && total == N * REFEICOES
                ? "OK: nenhum par de vizinhos comeu junto, todos comeram, sem deadlock."
                : "ERRO: inconsistencia detectada!");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
