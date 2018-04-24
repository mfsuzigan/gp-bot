FROM maven:latest
MAINTAINER Michel F. Suzigan
COPY target/gp-bot*.jar /usr/local/gp-bot/gp-bot.jar
COPY lib/ /usr/local/gp-bot/lib/
WORKDIR /usr/local/gp-bot
ENTRYPOINT java -jar gp-bot.jar

