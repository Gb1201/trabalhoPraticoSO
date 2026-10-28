package trabalhoPraticoSO.Codigos.barbeiroDominhoco;

import java.util.concurrent.Semaphore;

public class Main {

    static final int CADEIRAS = 3;
    static final int CLIENTES = 10;

    static final Semaphore customers = new Semaphore(0);
    static final Semaphore barbers = new Semaphore(0);
    static final Semaphore mutex = new Semaphore(1);
    static int waiting = 0;

    // servem como contadores na verificação
    static int atendidos = 0;
    static int desistentes = 0;

    static void log(String s) {
        System.out.println(s);
    }

    static class Barbeiro extends Thread {
        Barbeiro() { super("Barbeiro"); }

        public void run() {
            try {
                while (true) {
                    if (customers.availablePermits() == 0) {
                        log("[Barbeiro] Sem clientes... vai dormir.");
                    }
                    customers.acquire();          // caso nao tenha cliente, dorme
                    mutex.acquire();
                    waiting--;                    // um cliente sai da espera e vai pra cadeira
                    log("[Barbeiro] Chama um cliente. Esperando agora: " + waiting);
                    barbers.release();            // barbeiro pronto
                    mutex.release();
                    cortarCabelo();
                }
            } catch (InterruptedException e) {
                log("[Barbeiro] Fim do expediente.");
            }
        }

        void cortarCabelo() throws InterruptedException {
            log("[Barbeiro] Cortando cabelo...");
            Thread.sleep(300 + (int) (Math.random() * 300));
        }
    }

    static class Cliente extends Thread {
        final int id;
        Cliente(int id) { super("Cliente-" + id); this.id = id; }

        public void run() {
            try {
                mutex.acquire();
                if (waiting < CADEIRAS) {
                    waiting++;
                    log("[Cliente " + id + "] Chegou e sentou. Esperando: " + waiting + "/" + CADEIRAS);
                    customers.release();          // acorda o barbeiro ou entra na fila
                    mutex.release();
                    barbers.acquire();            // espera o barbeiro chamar
                    log("[Cliente " + id + "] Cortando o cabelo.");
                    Thread.sleep(300);
                    mutex.acquire(); atendidos++; mutex.release();
                    log("[Cliente " + id + "] Terminou e foi embora.");
                } else {
                    desistentes++;
                    log("[Cliente " + id + "] Barbearia CHEIA, foi embora sem cortar.");
                    mutex.release();
                }
            } catch (InterruptedException e) { }
        }
    }

    public static void executar() throws InterruptedException {
        waiting = 0; atendidos = 0; desistentes = 0;
        customers.drainPermits(); barbers.drainPermits();

        System.out.println("=== BARBEIRO DORMINHOCO (" + CADEIRAS + " cadeiras, " + CLIENTES + " clientes) ===");
        Barbeiro barbeiro = new Barbeiro();
        barbeiro.start();
        Thread.sleep(200);

        Cliente[] cs = new Cliente[CLIENTES];
        for (int i = 0; i < CLIENTES; i++) {
            cs[i] = new Cliente(i + 1);
            cs[i].start();
            Thread.sleep((int) (Math.random() * 250));
        }
        for (Cliente c : cs) c.join();

        Thread.sleep(400);
        barbeiro.interrupt();
        barbeiro.join();

        System.out.println("--- VALIDACAO ---");
        System.out.println("Atendidos: " + atendidos + " | Desistentes: " + desistentes
                + " | Total: " + (atendidos + desistentes) + " / " + CLIENTES);
        System.out.println(atendidos + desistentes == CLIENTES && waiting == 0
                ? "OK: todos os clientes foram contabilizados e a sala de espera terminou vazia."
                : "ERRO: inconsistencia detectada!");
    }

    public static void main(String[] args) throws InterruptedException {
        executar();
    }
}
