#!/bin/bash
cd /app
nohup fluent-bit -c /etc/fluent-bit/fluent-bit.conf &
java -Dspring.proriles.active=loc -jar pnc.jar
