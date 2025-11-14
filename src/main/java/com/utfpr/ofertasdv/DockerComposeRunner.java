package com.utfpr.ofertasdv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerComposeRunner {

    private static final Logger log = LoggerFactory.getLogger(DockerComposeRunner.class);
    private static final String DOCKER = "docker";

    public static void main(String[] args) {
        String cmd = (args.length == 0 || args[0].isBlank()) ? "up" : args[0];
        switch (cmd) {
            case "up":
                runCompose(new String[]{"up", "-d", "--build"});
                break;
            case "down":
                runCompose(new String[]{"down"});
                break;
            case "logs":
                streamAppLogs();
                break;
            case "version":
                showVersions();
                break;
            default:
                log.error("Comando desconhecido: {}", cmd);
                log.info("Uso: up | down | logs | version");
                System.exit(2);
        }
    }

    /** Constrói a linha de comando docker compose. Extraído para eventual teste unitário. */
    static List<String> buildComposeCommand(String[] subArgs) {
        List<String> cmd = new ArrayList<>();
        cmd.add(DOCKER);
        cmd.add("compose");
        for (String a : subArgs) {
            if (a != null && !a.isBlank()) {
                cmd.add(a);
            }
        }
        return cmd;
    }

    private static void runCompose(String[] subArgs) {
        List<String> command = buildComposeCommand(subArgs);
        log.info("Executando: {}", String.join(" ", command));
        int code = execAndPipe(command, Duration.ofMinutes(10));
        if (code != 0) {
            log.error("docker compose retornou código {}", code);
            System.exit(code);
        }
    }

    private static void streamAppLogs() {
        List<String> cmd = List.of(DOCKER, "logs", "-f", "ofertasdv-app");
        log.info("Logs do container ofertasdv-app (Ctrl+C para sair)...");
        execAndPipe(cmd, Duration.ofHours(12));
    }

    private static void showVersions() {
        log.info("Versões do Docker e variáveis relevantes:");
        // docker version
        execAndPipe(List.of(DOCKER, "version"), Duration.ofSeconds(30));
        String apiEnv = System.getenv("DOCKER_API_VERSION");
        if (apiEnv != null) {
            log.info("DOCKER_API_VERSION={} (se <1.44, atualize ou remova)", apiEnv);
        } else {
            log.info("DOCKER_API_VERSION não definida.");
        }
    }

    private static int execAndPipe(List<String> command, Duration timeout) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            Thread reader = new Thread(() -> {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        log.info(line);
                    }
                } catch (IOException e) {
                    log.error("Erro lendo saída: {}", e.getMessage());
                }
            }, "docker-compose-reader");
            reader.setDaemon(true);
            reader.start();
            long deadline = System.nanoTime() + timeout.toNanos();
            while (true) {
                try {
                    return p.waitFor();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    if (System.nanoTime() > deadline) {
                        log.error("Timeout executando comando: {}", String.join(" ", command));
                        p.destroyForcibly();
                        return 124; // Convenção: 124 = timeout (similar ao utilitário timeout)
                    }
                    // interrompido antes do deadline — continuar aguardando se ainda não finalizou
                }
            }
        } catch (IOException e) {
            log.error("Falha ao executar '{}': {}", String.join(" ", command), e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("No such file") && Objects.equals(command.get(0), DOCKER)) {
                log.error("Docker CLI não encontrado. Instale Docker ou ajuste PATH.");
            }
            return 127; // 127 = comando não encontrado
        }
    }
}
