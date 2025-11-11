#!/usr/bin/env bash
set -euo pipefail

# Script para subir todo o stack (app + postgres) com um comando
# Uso: ./run.sh [up|down|logs|rebuild]
# Default (sem argumentos): up

CMD=${1:-up}

case "$CMD" in
  up)
    echo "[OfertasDV] Subindo containers (build se necessario)..."
    docker compose up -d --build
    echo "[OfertasDV] Acesse: http://localhost:8080"
    echo "[OfertasDV] Logs: ./run.sh logs"
    ;;
  rebuild)
    echo "[OfertasDV] Rebuild forçado da imagem da aplicação..."
    docker compose build --no-cache
    docker compose up -d
    ;;
  logs)
    echo "[OfertasDV] Logs da aplicação (Ctrl+C para sair)..."
    docker logs -f ofertasdv-app
    ;;
  down)
    echo "[OfertasDV] Derrubando containers (mantendo volume de dados)..."
    docker compose down
    ;;
  down-all)
    echo "[OfertasDV] Derrubando containers e removendo volumes (DADOS SERÃO PERDIDOS!)..."
    docker compose down -v
    ;;
  psql)
    echo "[OfertasDV] Abrindo psql dentro do container do banco..."
    docker exec -it ofertasdv-db psql -U postgres -d ofertasdv
    ;;
  *)
    echo "Comando desconhecido: $CMD"
    echo "Uso: ./run.sh [up|down|down-all|logs|rebuild|psql]"
    exit 1
    ;;
 esac

