package trabalhoPraticoSO.Codigos.leitorEscritor;

import java.util.concurrent.Semaphore;


public class Main {

    static final int TAM = 5;
    static final int LEITORES = 3;
    static final int ESCRITORES = 2;
    static final int ITERACOES = 4;

    static final int[] M = new int[TAM];
    static final Semaphore db = new Semaphore(1);
    static final Semaphore mutex = new Semaphore(1);
    static int rc = 0;

    static volatile int leitoresAtivos = 0;   // so para validacao/log
    static volatile int escritoresAtivos = 0;
    static volatile boolean violacao = false;

    static class Escritor extends Thread {
        final int id;
        Escritor(int id) { super("Escritor-" + id); this.id = id; }

        public void run() {
            try {
                for (int k = 1; k <= ITERACOES; k++) {
                    Thread.sleep((int) (Math.random() * 300));
                    db.acquire();                          // acesso exclusivo
                    escritoresAtivos++;
                    if (leitoresAtivos > 0 || escritoresAtivos > 1) violacao = true;
                    int valor = id * 100 + k;
                    System.out.println("[Escritor " + id + "] ESCREVENDO " + valor + " em todo o vetor...");
                    for (int i = 0; i < TAM; i++) {
                        M[i] = valor;
                        Thread.sleep(20);                  // alarga a regiao critica
                    }
                    escritoresAtivos--;
                    System.out.println("[Escritor " + id + "] terminou. M=" + java.util.Arrays.toString(M));
                    db.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    static class Leitor extends Thread {
        final int id;
        Leitor(int id) { super("Leitor-" + id); this.id = id; }

        public void run() {
            try {
                for (int k = 1; k <= ITERACOES; k++) {
                    Thread.sleep((int) (Math.random() * 200));
                    mutex.acquire();
                    rc++;
                    if (rc == 1) db.acquire();             // primeiro leitor bloqueia escritores
                    mutex.release();

                    // ---- leitura (varios leitores simultaneos) ----
                    leitoresAtivos++;
                    if (escritoresAtivos > 0) violacao = true;
                    int[] copia = new int[TAM];
                    for (int i = 0; i < TAM; i++) {
                        copia[i] = M[i];
                        Thread.sleep(20);
                    }
                    boolean consistente = true;
                    for (int i = 1; i < TAM; i++) if (copia[i] != copia[0]) consistente = false;
                    if (!consistente) violacao = true;
                    System.out.println("[Leitor " + id + "] leu " + java.util.Arrays.toString(copia)
                            + (consistente ? "" : "  <-- INCONSISTENTE!")
                            + "  (leitores simultaneos: " + leitoresAtivos + ")");
                    leitoresAtivos--;

                    mutex.acquire();
                    rc--;
                    if (rc == 0) db.release();             // ultimo leitor libera escritores
                    mutex.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        java.util.Arrays.fill(M, 0);
        rc = 0; violacao = false; leitoresAtivos = 0; escritoresAtivos = 0;

        System.out.println("=== LEITORES E ESCRITORES (" + LEITORES + " leitores, " + ESCRITORES + " escritores) ===");
        Thread[] ts = new Thread[LEITORES + ESCRITORES];
        for (int i = 0; i < ESCRITORES; i++) ts[i] = new Escritor(i + 1);
        for (int i = 0; i < LEITORES; i++) ts[ESCRITORES + i] = new Leitor(i + 1);
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();

        System.out.println("--- VALIDACAO ---");
        System.out.println(violacao
                ? "ERRO: leitura inconsistente ou acesso simultaneo leitor/escritor detectado!"
                : "OK: nenhuma leitura inconsistente; escritas sempre exclusivas; leituras em paralelo.");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
