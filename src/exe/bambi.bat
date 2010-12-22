@REM Make sure prerequisite environment variables are set
@if not "%JAVA_HOME%" == "" goto gotJavaHome
@echo The JAVA_HOME environment variable is not defined
@echo This environment variable is needed to run this program
goto end
:gotJavaHome
@REM Setup Environment variables for JDFEditor
@REM Set any jars you want to appear at the begining of the classpath here
@REM remember to terminate with a ;

@SET PRE_JDFEDITOR_CLASSPATH=

@REM Set any jars you want to appear at the end of the classpath here
@REM remember to terminate with a ;

@SET POST_JDFEDITOR_CLASSPATH=

@SET LIB_DIR=lib
@SET CLASSPATH=.;%PRE_JDFEDITOR_CLASSPATH%;

@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/activation-1.0.2.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-lang-2.3.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-logging-1.0.4.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-io-2.0.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/log4j-1.2.8.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/mailapi.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/xercesImpl.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/xml-apis.jar;

@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-continuation-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-http-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-io-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-security-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-server-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-servlet-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-servlets-7.2.0.v20101020.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jetty-util-7.2.0.v20101020.jar;

@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-logging.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/servlet-api.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jsp-api.jar;


@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/JDFLibJ-2.1.4a.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/BambiCore.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-fileupload-1.2.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/Bambi.jar;

@SET CLASSPATH=%CLASSPATH%%POST_JDFEDITOR_CLASSPATH%

@"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.cip4.bambi.server.BambiServer %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
