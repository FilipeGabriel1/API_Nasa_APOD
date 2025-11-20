@echo off
echo Verificando se a porta 8080 esta em uso...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Porta 8080 em uso! Encerrando processo %%a...
    taskkill /PID %%a /F
    timeout /t 2 /nobreak >nul
)
echo Iniciando aplicacao...
call mvnw spring-boot:run
pause

