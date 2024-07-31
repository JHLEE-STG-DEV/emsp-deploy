FROM amazonlinux:2023

USER root

# Install JAVA
RUN yum install java-21-amazon-corretto-headless -y
RUN yum install java-21-amazon-corretto -y

# Install FluentBit & Config
RUN curl https://raw.githubusercontent.com/fluent/fluent-bit/master/install.sh | sh
COPY config/fluent-bit.conf /etc/fluent-bit/

# Location Source
RUN mkdir -p /app/log
ADD build/libs/ChargevOcpi-0.0.1-SNAPSHOT.jar /app/pnc.jar

# Execute FluentBit & PnC
COPY config/entrypoint.sh /app
RUN chmod +x /app/entrypoint.sh

WORKDIR /app
ENTRYPOINT ["/app/entrypoint.sh"]
