@echo off
REM build-run.cmd - Convenience script to build and run the book-service on Windows (cmd.exe)
REM Usage (positional args): build-run.cmd [skip-build] [port] [profile] [docker]
REM Examples:
REM   build-run.cmd                    -> build (using mvn or mvnw) and run produced jar
REM   build-run.cmd skip-build 8081    -> skip build, run existing jar on port 8081
REM   build-run.cmd skip-build 8081 dev-> skip build, run existing jar with 'dev' profile
REM   build-run.cmd docker 8081       -> build Docker image and run container mapping host port 8081 to container 8081

setlocal enabledelayedexpansion
:: Parse positional args
set ARG1=%~1
set ARG2=%~2
set ARG3=%~3
set ARG4=%~4
:: Defaults
set SKIP_BUILD=0
set DOCKER_MODE=0
set PORT=
set PROFILE=
:: Interpret args (simple positional rules):: If first arg is "skip-build" or "docker", treat accordingly
if /I "%ARG1%"=="skip-build" set SKIP_BUILD=1
if /I "%ARG1%"=="docker" set DOCKER_MODE=1
:: If first arg is not skip-build or docker and second is "docker" (support other order)
if /I "%ARG2%"=="docker" set DOCKER_MODE=1
:: Port can be provided as the first non-keyword numeric arg; check ARG1..ARG3n:: Helper to detect numeric-ish value
for %%A in ("%ARG1%" "%ARG2%" "%ARG3%") do (
  for /f "delims=" %%V in ("%%~A") do (
    if not defined PORT (
      rem check if value looks like a number (basic check)
      echo %%~V | findstr /R "^[0-9][0-9]*$" >nul 2>&1
      if !errorlevel! EQU 0 (
        set PORT=%%~V
        goto :FOUND_PORT
      )
    )
  )
)
:FOUND_PORT
:: Profile detection: any remaining arg that is not skip-build, docker or a numeric port is treated as profile (first such)for %%A in ("%ARG1%" "%ARG2%" "%ARG3%") do (
  set VAL=%%~A
  if /I "!VAL!" NEQ "skip-build" if /I "!VAL!" NEQ "docker" (
    echo !VAL! | findstr /R "^[0-9][0-9]*$" >nul 2>&1
    if !errorlevel! NEQ 0 (
      if not defined PROFILE set PROFILE=!VAL!
    )
  )
)
:: If ARG4 explicitly equals docker, enable docker modeif /I "%ARG4%"=="docker" set DOCKER_MODE=1
:: Show summarynecho =============================necho Book Service build-run helpernecho SKIP_BUILD=%SKIP_BUILD%necho DOCKER_MODE=%DOCKER_MODE%necho PORT=%PORT%necho PROFILE=%PROFILE%necho =============================:: If docker mode requested -> build and run containerif %DOCKER_MODE%==1 (  where docker >nul 2>&1 || (    echo ERROR: Docker CLI not found on PATH. Install Docker Desktop or add docker to PATH.& exit /b 1  )  echo Building Docker image 'book-service:local'...  docker build -t book-service:local . || (    echo Docker build failed.& exit /b 1  )  if "%PORT%"=="" set PORT=8081  echo Running container mapping host port %PORT% to container 8081...  docker run --rm -p %PORT%:8081 --name book-service book-service:local  exit /b %ERRORLEVEL%):: Ensure Java available for running the jarwhere java >nul 2>&1 || (  echo ERROR: Java not found on PATH. Please install Java 17 or set PATH accordingly.& exit /b 1):: Build (unless skipping)if %SKIP_BUILD%==0 (  echo Building with Maven (skipping tests)...  where mvn >nul 2>&1  if !errorlevel! NEQ 0 (    if exist mvnw (      echo Using project mvnw...      call mvnw -DskipTests clean package || (        echo Maven wrapper build failed.& exit /b 1      )    ) else (      echo Maven CLI not found on PATH. Please install Maven or use the provided mvnw.& exit /b 1    )  ) else (    mvn -DskipTests clean package || (      echo Maven build failed.& exit /b 1    )  )) else (  echo Skipping build as requested.):: Locate produced jar in target\*.jar (pick first match)set JAR_PATH=for %%F in (target\*.jar) do (  if not defined JAR_PATH set JAR_PATH=%%~fF)if not defined JAR_PATH (  echo ERROR: No jar file found in target\ directory. Build may have failed or package name differs.& exit /b 1):: Build java command lineset CMD_LINE=java -jar "%JAR_PATH%"if defined PORT set CMD_LINE=%CMD_LINE% --server.port=%PORT%if defined PROFILE set CMD_LINE=%CMD_LINE% --spring.profiles.active=%PROFILE%echo Executing: %CMD_LINE%%CMD_LINE%endlocalexit /b %ERRORLEVEL%