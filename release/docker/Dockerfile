FROM airvantage/av-java

EXPOSE 8080

ENTRYPOINT /usr/bin/java -DAVsched_CONF_DIR=/etc/av-sched/ -Dlogback.configurationFile=/etc/av-sched/logback.xml -jar *.jar