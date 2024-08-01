#!/bin/bash
cd /app
nohup /opt/fluent-bit/bin/fluent-bit -c /etc/fluent-bit/fluent-bit.conf &
java -Dspring.proriles.active=loc -jar pnc.jar
