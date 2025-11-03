.PHONY: help up down setup restart logs clean test

help:
    @echo "Available commands:"
    @echo "  make up       - Start Kong Gateway"
    @echo "  make setup    - Configure Kong (run after 'up')"
    @echo "  make down     - Stop Kong Gateway"
    @echo "  make restart  - Restart Kong Gateway"
    @echo "  make logs     - View Kong logs"
    @echo "  make clean    - Remove all data and restart"
    @echo "  make test     - Test Kong connection"

up:
    docker-compose up -d
    @echo "Waiting for Kong to be ready..."
    @sleep 15
    @echo "Kong is ready! Run 'make setup' to configure."

setup:
    @powershell -ExecutionPolicy Bypass -File kong-setup.ps1

down:
    docker-compose down

restart:
    docker-compose restart

logs:
    docker-compose logs -f kong

clean:
    docker-compose down -v
    docker-compose up -d
    @sleep 20
    @powershell -ExecutionPolicy Bypass -File kong-setup.ps1

test:
    @curl -s http://localhost:8001/status | jq