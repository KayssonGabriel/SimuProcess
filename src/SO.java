import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;

class SO {
    private LinkedList<Processo> tabelaDeProcessos;
    private final int QUANTUM = 1000;
    private final Random random = new Random();

    public SO() {
        tabelaDeProcessos = new LinkedList<>();
        inicializarProcessos();
        limparArquivo(); // Limpa o arquivo de logs no início da simulação
    }

    private void inicializarProcessos() {
        int[] temposExecucao = {10000, 5000, 7000, 3000, 3000, 8000, 2000, 5000, 4000, 10000};
        for (int i = 0; i < temposExecucao.length; i++) {
            tabelaDeProcessos.add(new Processo(i, temposExecucao[i]));
        }
    }

    // Limpa o arquivo de logs para uma nova execução
    private void limparArquivo() {
        try (PrintWriter writer = new PrintWriter("tabela_processos.txt")) {
            writer.print("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executar() {
        while (!tabelaDeProcessos.isEmpty()) {
            // Verificar processos BLOQUEADOS para ver se voltam a PRONTO
            for (Processo p : tabelaDeProcessos) {
                if (p.estado.equals("BLOQUEADO") && random.nextInt(100) < 30) {
                    p.estado = "PRONTO";
                    imprimirEstado(p, "BLOQUEADO >>> PRONTO");
                    salvarProcesso(p); // Salva o estado após troca de contexto para PRONTO
                }
            }

            // Executar o próximo processo PRONTO impedindo que processos BLOQUEADOS ou FINALIZADOS sejam movidos para o estado EXECUTANDO
            Processo processo = tabelaDeProcessos.getFirst();
            if (!processo.estado.equals("PRONTO")) {
                tabelaDeProcessos.add(tabelaDeProcessos.removeFirst()); // Move para o final da fila
                continue;
            }

            // Reiniciar o quantum para o processo em execução
            processo.estado = "EXECUTANDO";
            processo.n_cpu++; // Incrementa o contador de uso da CPU
            imprimirEstado(processo, "PRONTO >>> EXECUTANDO");
            salvarProcesso(processo); // Salva o estado após troca de contexto para EXECUTANDO

            for (int ciclo = 0; ciclo < QUANTUM; ciclo++) {
                processo.tp++;
                processo.atualizarCP();
                processo.tempoRestante--;

                if (random.nextInt(100) < 1) { // 1% chance de E/S
                    processo.estado = "BLOQUEADO";
                    processo.nes++; // Incrementa o contador de operações de E/S
                    imprimirEstado(processo, "EXECUTANDO >>> BLOQUEADO");
                    salvarProcesso(processo); // Salva o estado após troca de contexto para BLOQUEADO
                    break;
                }

                if (processo.tempoRestante <= 0) { // Processo finalizado
                    imprimirEstado(processo, "FINALIZADO");
                    salvarProcesso(processo); // Grava os dados finais do processo
                    tabelaDeProcessos.removeFirst(); // Remove o processo da lista após finalização
                    break;
                }

                // Verificar troca de contexto ao final do quantum se ainda estiver EXECUTANDO
                if (ciclo == QUANTUM - 1 && processo.estado.equals("EXECUTANDO")) {
                    processo.estado = "PRONTO";
                    imprimirEstado(processo, "EXECUTANDO >>> PRONTO");
                    salvarProcesso(processo); // Salva o estado após troca de contexto para PRONTO
                }
            }

            // Mover o processo para o final da lista, caso não tenha terminado e esteja em PRONTO
            if (processo.tempoRestante > 0 && processo.estado.equals("PRONTO")) {
                tabelaDeProcessos.add(tabelaDeProcessos.removeFirst());
            }
        }
    }

    private void imprimirEstado(Processo processo, String transicao) {
        System.out.println(processo + " Transição: " + transicao);
    }

    private void salvarProcesso(Processo processo) {
        // Salva o estado atualizado do processo no arquivo
        try (FileWriter writer = new FileWriter("tabela_processos.txt", true)) {
            writer.write(processo.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}