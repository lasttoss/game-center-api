SHELL := /bin/bash
COMPOSE ?= docker compose

.PHONY: help up down logs build run test clean diagram

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-8s\033[0m %s\n", $$1, $$2}'

up: ## Start mongo + redis + the api
	$(COMPOSE) up -d --build
	@echo "api      : http://localhost:$${API_PORT:-8080}/api/v1"
	@echo "swagger  : http://localhost:$${API_PORT:-8080}/swagger-ui.html"

down: ## Stop the stack
	$(COMPOSE) down

logs: ## Tail the api log
	$(COMPOSE) logs -f api

build: ## Package the jar
	./mvnw -B -DskipTests package

run: ## Run locally (needs mongo + redis on localhost)
	./mvnw -B spring-boot:run

test: ## Tests
	./mvnw -B test

clean: ## Remove build output and volumes
	./mvnw -B clean
	$(COMPOSE) down -v

# Sources are HTML and Mermaid; a PNG is a build artifact.
diagram:
	@if command -v chromium >/dev/null 2>&1; then B=chromium; elif command -v google-chrome >/dev/null 2>&1; then B=google-chrome; else echo "no chromium on PATH: open docs/diagrams/*.html in a browser"; exit 0; fi; \
	for f in docs/diagrams/*.html; do $$B --headless --screenshot="$${f%.html}.png" --window-size=1200,1000 "$$f" && echo "wrote $${f%.html}.png"; done
