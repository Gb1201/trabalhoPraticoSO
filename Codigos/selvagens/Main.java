package trabalhoPraticoSO.Codigos.selvagens;

import java.util.concurrent.Semaphore;

public class Main {

    static final int PORCOES = 3;
    static final int SELVAGENS = 5;
    static final int REFEICOES = 3;

    static final Semaphore mutex = new Semaphore(1);
    static final Semaphore caldeiraoVazio = new Semaphore(0);
    static final Semaphore caldeiraoCheio = new Semaphore(0);

    static int porcoes = 0;
    static int servidas = 0;
    static int enchidas = 0;
    static volatile boolean violacao = false;

    static class Cozinheiro extends Thread {
        Cozinheiro() { super("Cozinheiro"); }
        public void run() {
            try {
                while (true) {
                    System.out.println("[Cozinheiro] dormindo...");
                    caldeiraoVazio.acquire();                 // dorme ate ser acordado
                    if (porcoes != 0) violacao = true;        // so enche se vazio
                    System.out.println("[Cozinheiro] acordou! Enchendo o caldeirao.");
                    Thread.sleep(200);
                    porcoes = PORCOES;
                    enchidas++;
                    System.out.println("[Cozinheiro] caldeirao cheio (" + PORCOES + " porcoes).");
                    caldeiraoCheio.release();
                }
            } catch (InterruptedException e) {
                System.out.println("[Cozinheiro] fim do jantar.");
            }
        }
    }

    static class Selvagem extends Thread {
        final int id;
        Selvagem(int id) { super("Selvagem-" + id); this.id = id; }
        public void run() {
            try {
                for (int k = 1; k <= REFEICOES; k++) {
                    mutex.acquire();                          // regiao critica: servir
                    if (porcoes == 0) {
                        System.out.println("[Selvagem " + id + "] caldeirao VAZIO! Acorda o cozinheiro.");
                        caldeiraoVazio.release();
                        caldeiraoCheio.acquire();             // espera encher
                    }
                    if (porcoes <= 0) violacao = true;
                    porcoes--;
                    servidas++;
                    System.out.println("[Selvagem " + id + "] se serviu. Restam " + porcoes + " porcoes.");
                    mutex.release();

                    System.out.println("   [Selvagem " + id + "] comendo (refeicao " + k + ")...");
                    Thread.sleep(100 + (int) (Math.random() * 300));
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        servidas = 0; enchidas = 0; violacao = false;
        mutex.drainPermits(); mutex.release();
        caldeiraoVazio.drainPermits(); caldeiraoCheio.drainPermits();
        porcoes = PORCOES;                                    // caldeirao comeca cheio

        System.out.println("=== SELVAGENS (" + SELVAGENS + " selvagens, caldeirao com " + PORCOES + " porcoes) ===");
        Cozinheiro c = new Cozinheiro();
        c.start();
        Selvagem[] ss = new Selvagem[SELVAGENS];
        for (int i = 0; i < SELVAGENS; i++) { ss[i] = new Selvagem(i + 1); ss[i].start(); }
        for (Selvagem s : ss) s.join();
        c.interrupt();
        c.join();

        int esperado = SELVAGENS * REFEICOES;
        System.out.println("--- VALIDACAO ---");
        System.out.println("Porcoes servidas: " + servidas + " / " + esperado + " | Vezes que o cozinheiro encheu: " + enchidas);
        System.out.println(servidas == esperado && !violacao
                ? "OK: ninguem se serviu com o caldeirao vazio e o cozinheiro so encheu quando vazio."
                : "ERRO: inconsistencia detectada!");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
