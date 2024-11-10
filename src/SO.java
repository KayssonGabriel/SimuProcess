import java.io.*;
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

    private void limparArquivo() {
        try (PrintWriter writer = new PrintWriter("tabela_processos.txt")) {
            writer.print("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void executar() {
        while (!tabelaDeProcessos.isEmpty()) {
            // Verifica processos BLOQUEADOS, chance de 30% de transição para PRONTO
            for (Processo p : tabelaDeProcessos) {
                if (p.estado.equals("BLOQUEADO") && random.nextInt(100) < 30) {
                    p.estado = "PRONTO";
                    imprimirEstado(p, "BLOQUEADO >>> PRONTO");
                    salvarProcesso(p);
                }
            }

            // Pega o primeiro processo da fila
            Processo processo = tabelaDeProcessos.getFirst();
            if (!processo.estado.equals("PRONTO")) {
                tabelaDeProcessos.add(tabelaDeProcessos.removeFirst());
                continue;
            }

            processo.estado = "EXECUTANDO";
            processo.n_cpu++;
            imprimirEstado(processo, "PRONTO >>> EXECUTANDO");
            salvarProcesso(processo);

            // Começa a execução do processo no quantum
            for (int ciclo = 0; ciclo < QUANTUM; ciclo++) {
                if (processo.tempoRestante <= 0) {
                    processo.estado = "FINALIZADO";
                    imprimirEstado(processo, "EXECUTANDO >>> FINALIZADO");
                    salvarProcesso(processo);
                    tabelaDeProcessos.removeFirst(); // Remove o processo da lista após a finalização
                    break;
                }

                processo.tp++;
                processo.atualizarCP();
                processo.tempoRestante--;  // Decrementa apenas se o tempoRestante for > 0

                // Verifica se o processo faz uma operação de E/S (1% de chance)
                if (random.nextInt(100) < 1) { // 1% chance de E/S
                    processo.estado = "BLOQUEADO";
                    processo.nes++;
                    imprimirEstado(processo, "EXECUTANDO >>> BLOQUEADO");
                    salvarProcesso(processo);
                    break;
                }

                // Se o quantum for completado, coloca o processo de volta na fila de prontos
                if (ciclo == QUANTUM - 1 && processo.estado.equals("EXECUTANDO")) {
                    processo.estado = "PRONTO";
                    imprimirEstado(processo, "EXECUTANDO >>> PRONTO");
                    salvarProcesso(processo);
                }

                // Pausa de 1 ms entre os ciclos
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Se o processo ainda tem tempo restante, coloca-o de volta na fila de prontos
            if (processo.tempoRestante > 0 && processo.estado.equals("PRONTO")) {
                tabelaDeProcessos.add(tabelaDeProcessos.removeFirst());
            }
        }
    }


    private void imprimirEstado(Processo processo, String transicao) {
        System.out.println(processo.toStringParaTerminal() + " Transição: " + transicao);
    }

    private void salvarProcesso(Processo processo) {
        LinkedList<String> linhas = new LinkedList<>();
        String cabecalho = String.format("%-5s %-8s %-8s %-10s %-6s %-8s %-15s",
                "PID", "TP", "CP", "Estado", "NES", "N_CPU", "Tempo Restante");

        try (BufferedReader reader = new BufferedReader(new FileReader("tabela_processos.txt"))) {
            String linha;
            boolean isPrimeiraLinha = true;
            while ((linha = reader.readLine()) != null) {
                if (isPrimeiraLinha && linha.equals(cabecalho)) {
                    isPrimeiraLinha = false;
                }
                linhas.add(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (linhas.isEmpty() || !linhas.getFirst().equals(cabecalho)) {
            linhas.addFirst(cabecalho);
        }

        boolean processoAtualizado = false;
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            if (linha.startsWith(String.format("%-5d", processo.pid))) {
                linhas.set(i, processo.toStringParaArquivo());
                processoAtualizado = true;
                break;
            }
        }

        if (!processoAtualizado) {
            linhas.add(processo.toStringParaArquivo());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("tabela_processos.txt", false))) {
            for (String linha : linhas) {
                writer.println(linha);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
