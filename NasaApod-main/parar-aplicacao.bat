@echo off
echo Procurando processos na porta 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Encerrando processo %%a...
    taskkill /PID %%a /F
)
echo Pronto! Porta 8080 liberada.
pause

