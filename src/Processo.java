class Processo {
    int pid;
    int tp; // Tempo de processamento total executado
    int cp; // Contador de Programa
    String estado;
    int nes; // Número de vezes que realizou operação de E/S
    int n_cpu; // Número de vezes que usou a CPU
    int tempoRestante; // Ciclos restantes para completar o processo

    public Processo(int pid, int tempoRestante) {
        this.pid = pid;
        this.tp = 0;
        this.cp = tp + 1;
        this.estado = "PRONTO";
        this.nes = 0;
        this.n_cpu = 0;
        this.tempoRestante = tempoRestante;
    }

    public void atualizarCP() {
        this.cp = this.tp + 1;
    }

    public String toStringParaArquivo() {
        return String.format("%-5d %-8d %-8d %-10s %-6d %-8d %-15d",
                pid, tp, cp, estado, nes, n_cpu, tempoRestante);
    }

    public String toStringParaTerminal() {
        return "PID: " + pid +
                ", TP: " + tp +
                ", CP: " + cp +
                ", Estado: " + estado +
                ", NES: " + nes +
                ", N_CPU: " + n_cpu +
                ", Tempo Restante: " + tempoRestante;
    }
}
