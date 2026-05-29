@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE (SET "BASE_DIR=%__MVNW_ARG0_NAME__%\")
@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
@SET DOWNLOAD_URL=%WRAPPER_URL%
FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)
@IF EXIST %WRAPPER_JAR% (
    SET MVNW_VERBOSE=false
    IF NOT "%MVNW_VERBOSE%"=="true" (
        @java -cp %WRAPPER_JAR% %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
    ) ELSE (
        @java -cp %WRAPPER_JAR% %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
    )
) ELSE (
    @IF NOT "%MVNW_REPOURL%"=="" SET DOWNLOAD_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    @echo Downloading from: %DOWNLOAD_URL%
    @powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%'"
    @java -cp %WRAPPER_JAR% %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
)
