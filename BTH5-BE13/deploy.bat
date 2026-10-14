@echo off
chcp 65001 >nul
echo ========================================================
echo DONG GOI VA TRIEN KHAI DU AN BTH5 LEN TOMCAT
echo [Bai tap] Goi MySQL Stored Procedures tu JDBC
echo ========================================================

if "%JAVA_HOME%"=="" set JAVA_HOME=C:\Users\ADMIN\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.12.101-hotspot
if "%CATALINA_HOME%"=="" set CATALINA_HOME=C:\Users\ADMIN\apache-tomcat-10.1.60

echo 1. Bien dich du an voi Maven...
call "C:\Users\ADMIN\.maven\maven-3.10.0\bin\mvn.cmd" clean package
if %ERRORLEVEL% neq 0 (
    echo [LOI] Bien dich that bai!
    pause
    exit /b %ERRORLEVEL%
)

echo 2. Trien khai file .war vao Tomcat webapps...
if exist "C:\Users\ADMIN\apache-tomcat-10.1.60\webapps\user-management" (
    rmdir /s /q "C:\Users\ADMIN\apache-tomcat-10.1.60\webapps\user-management"
)
copy /Y "target\user-management.war" "C:\Users\ADMIN\apache-tomcat-10.1.60\webapps\"

echo 3. Khoi dong / Tai lai Tomcat...
start "Tomcat Server" "C:\Users\ADMIN\apache-tomcat-10.1.60\bin\startup.bat"

echo ========================================================
echo Trien khai hoan tat!
echo Truy cap ung dung tai: http://localhost:8080/user-management/users
echo ========================================================
