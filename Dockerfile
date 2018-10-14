FROM selenium/standalone-chrome
MAINTAINER Michel F. Suzigan
COPY release/gp-bot/ /usr/local/gp-bot/
WORKDIR /usr/local/gp-bot
USER root
ENV TZ America/Sao_Paulo
ENTRYPOINT ["java", "-jar", "gp-bot.jar"]
CMD [""]