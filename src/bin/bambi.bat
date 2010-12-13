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

@SET LIB_DIR=../lib
@SET CLASSPATH=.;%PRE_JDFEDITOR_CLASSPATH%;

@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/activation-1.0.2.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-lang-2.1.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-logging-1.0.4.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/commons-io-1.1.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/log4j-1.2.8.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/mailapi.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/xercesImpl.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/xml-apis.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/rsyntaxtextarea.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/tcpmon.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jhall.jar;

@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/JDFLibJ-2.1.4a.jar;
@SET CLASSPATH=%CLASSPATH%%LIB_DIR%/jdfeditor.jar;

@SET CLASSPATH=%CLASSPATH%%POST_JDFEDITOR_CLASSPATH%

@"%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" org.cip4.jdfeditor.Editor %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
