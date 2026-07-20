#!/bin/bash

export KAFKA_NODE_ID=${KAFKA_NODE_ID:-1}
export KAFKA_PROCESS_ROLES=broker,controller
export KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093
export KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://${KAFKA_ADVERTISED_HOST:-kafka}:9092
export KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
export KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER
export KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093
export KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
export KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
export KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
export KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1
export KAFKA_LOG_DIRS=/tmp/kraft-combined-logs

# Format storage if required
if [ ! -f "/tmp/kraft-combined-logs/meta.properties" ]; then
    CLUSTER_ID=$(/opt/kafka/bin/kafka-storage.sh random-uuid)
    /opt/kafka/bin/kafka-storage.sh format \
        --ignore-formatted \
        --cluster-id $CLUSTER_ID \
        --config /opt/kafka/config/kraft/server.properties
fi

# Start Kafka server in background
/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/kraft/server.properties &
KAFKA_PID=$!

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
sleep 10

# Create topics
echo "Creating Kafka topics..."
/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic join-request-status-topic \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

/opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic payment-success-topic \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

echo "Topics created successfully!"

# Keep container running
wait $KAFKA_PID