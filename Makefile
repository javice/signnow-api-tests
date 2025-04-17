
# Variables para colores
GREEN := \033[0;32m
RED := \033[0;31m
YELLOW := \033[0;33m
NC := \033[0m # No Color

.PHONY: run-tests report clean install build run coverage coverage-report verify-env debug-env

run-tests:
	@echo "$(YELLOW)Ejecutando tests...$(NC)"
	@./gradlew clean test && echo "$(GREEN)Tests completados satisfactoriamente!$(NC)" || echo "$(RED)Tests fallidos!! Por favor revisa la salida del log.$(NC)"

# Añadido nuevo comando para ejecutar tests con cobertura
coverage:
	@echo "$(YELLOW)Ejecutando tests con análisis de cobertura...$(NC)"
	@./gradlew clean test jacocoTestReport && echo "$(GREEN)Tests con cobertura completados satisfactoriamente!$(NC)" || echo "$(RED)Generación de cobertura fallida!! Por favor revisa la salida del log.$(NC)"

# Añadido comando para abrir el informe de cobertura HTML
coverage-report:
	@echo "$(YELLOW)Abriendo reporte de cobertura...$(NC)"
	@open build/reports/jacoco/test/html/index.html && echo "$(GREEN)Reporte de cobertura abierto correctamente!$(NC)" || echo "$(RED)No se pudo abrir el reporte de cobertura.$(NC)"

report:
	@echo "$(YELLOW)Abriendo reporte...$(NC)"
	@open report.html && echo "$(GREEN)Reporte abierto correctamente!$(NC)"

clean:
	@echo "$(YELLOW)Limpiando...$(NC)"
	@./gradlew clean && echo "$(GREEN)Limpiado correctamente!$(NC)"

install:
	@echo "$(YELLOW)Instalando...$(NC)"
	@./gradlew installDist && echo "$(GREEN)Instalado correctamente!$(NC)"

build:
	@echo "$(YELLOW)Compilando...$(NC)"
	@./gradlew build && echo "$(GREEN)Compilado correctamente!$(NC)"

run:
	@echo "$(YELLOW)Ejecutando...$(NC)"
	@./gradlew run && echo "$(GREEN)Ejecutado correctamente!$(NC)"
