#!/bin/sh
#Make sure prerequisite environment variables are set
if [ "JAVA_HOME" = "" ]
then
    echo "The JAVA_HOME environment variable is not defined"
    echo "This environment variable is needed to run this program"
fi

#Setup Environment variables for JDFEditor
# Set any jars you want to appear at the begining of the classpath here
# remember to terminate with a :

PRE_CLASSPATH=

#Set any jars you want to appear at the end of the classpath here
#remember to terminate with a :

POST_CLASSPATH=

LIB_DIR=../lib
CLASSPATH=.:$PRE_CLASSPATH:

CLASSPATH=$CLASSPATH$LIB_DIR/activation-1.0.2.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/commons-lang-2.3.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/commons-logging.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/commons-io-2.0.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/log4j-1.2.8.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/mailapi.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/xercesImpl.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/xml-apis.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-continuation-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-http-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-io-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-security-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-server-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-servlet-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-servlets-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/jetty-util-7.2.0.v20101020.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/JDFLibJ-2.1.4a.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/BambiCore.jar:
CLASSPATH=$CLASSPATH$LIB_DIR/Bambi.jar:

CLASSPATH=$CLASSPATH$POST_CLASSPATH
echo $CLASSPATH

#$JAVA_HOME/bin/java -Dapple.laf.useScreenMenuBar=true -classpath $CLASSPATH org.cip4.bambi.server.BambiServer $@
java -v -Dapple.laf.useScreenMenuBar=true -classpath $CLASSPATH org.cip4.bambi.server.BambiServer $@

