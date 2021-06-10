cd src
set MAVEN_OPTS=
@call mvn clean dependency:copy@get-lombok
set MAVEN_OPTS=-javaagent:target/lombok.jar=ECJ
@call mvn verify -PSDK,find-bugs