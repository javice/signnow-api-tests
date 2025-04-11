# SignNow API Tests 🚀

Tests API automatizados para el flujo OAuth2 utilizando `Rest Assured` y `TestNG`.

## Tabla de Contenidos 📚
- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Instalación](#instalación)
- [Ejecutar pruebas](#ejecutar-pruebas)
- [Ver reporte](#ver-reporte)
- [CI/CD](#ci-cd)
- [Contribuciones](#contribuciones)
- [Licencia](#licencia)

## Configuración ⚙️

Configuración de variables de entorno en el archivo `.env` en la raiz del proyecto.

```bash
CLIENT_ID=...
CLIENT_SECRET=...
```

## Instalación 💻
make install

## Requisitos 📋
- Java 17+
- Gradle
- IntelliJ
- Variables de entorno: `CLIENT_ID`, `CLIENT_SECRET`

## Ejecutar pruebas 🏃‍♂️

```bash
make run-tests
```
Para ejecutar pruebas específicas, puedes usar el siguiente comando: make run-tests <nombre_de_la_prueba>.


## Ver reporte 📊

```bash
make report
```
Los reportes se generarán en el directorio reports. Puedes abrir el archivo report.html para ver los resultados.


## CI/CD 🔄

GitHub Actions ejecuta las pruebas automáticamente en cada push o PR. Esto ayuda a mantener la calidad del código al asegurarse de que todas las pruebas pasen antes de fusionar cambios.

## Contribuciones  🤝
Si deseas contribuir, por favor abre un issue o envía un pull request. Asegúrate de seguir nuestras pautas de contribución.

## Licencia 📄
Este proyecto está bajo la Licencia MIT. Consulta el archivo LICENSE para más detalles.




