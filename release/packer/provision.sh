#! /bin/bash

# Install av-sched service
sudo docker pull airvantage/av-sched:$APP_VERSION
sudo docker tag airvantage/av-sched:$APP_VERSION airvantage/av-sched:current
sudo mkdir /etc/av-sched
sudo mv /tmp/logback.xml /etc/av-sched/logback.xml
sudo mv /tmp/av-sched.service /etc/systemd/system/av-sched.service
sudo systemctl enable /etc/systemd/system/av-sched.service
