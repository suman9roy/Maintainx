FROM apache/kafka:3.9.0

LABEL maintainer="Suman Roy"
LABEL project="MaintainX"

EXPOSE 9092

COPY start-kafka.sh /opt/start-kafka.sh

USER root
RUN sed -i 's/\r$//' /opt/start-kafka.sh && chmod +x /opt/start-kafka.sh

ENTRYPOINT ["/opt/start-kafka.sh"]