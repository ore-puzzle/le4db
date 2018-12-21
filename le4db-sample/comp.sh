javac -d WEB-INF/classes -cp servlet-api-3.1.jar:WEB-INF/lib/*.jar WEB-INF/src/*.java
javac -cp servlet-api-3.1.jar:jetty-all-9.4.9.v20180320-uber.jar StartJetty.java
java -cp .:servlet-api-3.1.jar:jetty-all-9.4.9.v20180320-uber.jar StartJetty
