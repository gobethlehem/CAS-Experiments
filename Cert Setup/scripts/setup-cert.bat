REM http://www.jasig.org/cas/server-deployment/solving-ssl-issues

"%JAVA_HOME%\bin\keytool" -delete -alias tomcat -keypass changeit

"%JAVA_HOME%\bin\keytool" -genkey -alias tomcat -keypass changeit -keyalg RSA

"%JAVA_HOME%\bin\keytool" -export -alias tomcat -keypass changeit  -file tomcat.crt

"%JAVA_HOME%\bin\keytool" -delete -alias tomcat -keypass changeit -keystore "%JAVA_HOME%/jre/lib/security/cacerts"

"%JAVA_HOME%\bin\keytool" -import -file tomcat.crt -keypass changeit -alias tomcat -keystore "%JAVA_HOME%/jre/lib/security/cacerts"
