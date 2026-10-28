package trabalhoPraticoSO;

import java.util.Scanner;

/**
 * Menu geral: permite escolher qual problema clássico de sincronização executar.
 */
public class MainGeral {

    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        int op = -1;
        while (op != 0) {
            System.out.println();
            System.out.println("==========================================");
            System.out.println("  PROBLEMAS CLASSICOS DE SINCRONIZACAO");
            System.out.println("==========================================");
            System.out.println(" 1 - Barbeiro Dorminhoco");
            System.out.println(" 2 - Leitores e Escritores");
            System.out.println(" 3 - Fumantes");
            System.out.println(" 4 - Filosofos");
            System.out.println(" 5 - Jantar dos Selvagens");
            System.out.println(" 6 - Tres Robos (Bart, Lisa, Maggie)");
            System.out.println(" 0 - Sair");
            System.out.print("Escolha: ");

            String linha = in.hasNextLine() ? in.nextLine().trim() : "0";
            try {
                op = Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                op = -1;
            }

            System.out.println();
            switch (op) {
                case 1 -> trabalhoPraticoSO.Codigos.barbeiroDominhoco.Main.executar();
                case 2 -> trabalhoPraticoSO.Codigos.leitorEscritor.Main.executar();
                case 3 -> trabalhoPraticoSO.Codigos.fumantes.Main.executar();
                case 4 -> trabalhoPraticoSO.Codigos.filosofos.Main.executar();
                case 5 -> trabalhoPraticoSO.Codigos.selvagens.Main.executar();
                case 6 -> trabalhoPraticoSO.Codigos.tresrobos.Main.executar();
                case 0 -> System.out.println("Encerrando.");
                default -> System.out.println("Opcao invalida.");
            }
        }
    }
}
