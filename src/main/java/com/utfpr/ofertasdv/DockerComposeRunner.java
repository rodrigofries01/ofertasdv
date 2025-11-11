package com.utfpr.ofertasdv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Runner simples para acionar o script run.sh via botão "play" no IDE.
 * Opções de argumento (primeiro argumento): up (default), logs, down.
 * Exemplo: Run Configuration -> Program arguments: logs
 */
public class DockerComposeRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        String action = args.length > 0 ? args[0] : "up";
        if (!Arrays.asList("up","logs","down").contains(action)) {
            System.err.println("Ação inválida: " + action + " (use up|logs|down)");
            System.exit(2);
        }

        File script = new File("run.sh");
        if (!script.exists()) {
            System.err.println("Script run.sh não encontrado no diretório: " + script.getAbsolutePath());
            System.exit(1);
        }
        if (!script.canExecute()) {
            // Tenta tornar executável
            script.setExecutable(true);
        }

        ProcessBuilder pb = new ProcessBuilder("bash", "./run.sh", action);
        pb.inheritIO();
        System.out.println("[Runner] Executando ./run.sh " + action + " ...");
        Process p = pb.start();
        int code = p.waitFor();
        System.out.println("[Runner] Finalizado com código: " + code);
        if (code != 0) {
            System.exit(code);
        }
    }
}

