# KAFKA SETUP locally
Run init_kafka.sh

## Commands related to topic

```sh
#!/bin/bash

# Delete all topics
echo "Deleting all topics..."
kafka-topics --bootstrap-server localhost:9092 --list | grep -v "^__" | xargs -r -I {} kafka-topics --bootstrap-server localhost:9092 --delete --topic {}

# Wait for deletion
sleep 2

# Verify topics are deleted
echo "Remaining topics:"
kafka-topics --bootstrap-server localhost:9092 --list

# Create a new test topic
kafka-topics --create --topic my-topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

echo "Setup complete. New topic created."

# List all topics and delete them
# kafka-topics --bootstrap-server localhost:9092 --list | xargs -I {} kafka-topics --bootstrap-server localhost:9092 --delete --topic {}

# First, list all topics
# kafka-topics --bootstrap-server localhost:9092 --list

# Delete each topic individually
# kafka-topics --bootstrap-server localhost:9092 --delete --topic topic-name

# Delete a specific topic
# kafka-topics --bootstrap-server localhost:9092 --delete --topic test-topic

# Delete multiple specific topics
# kafka-topics --bootstrap-server localhost:9092 --delete --topic topic1,topic2,topic3
```

## Command related to offset

```sh
# Consumer starts from latest messages (newly produced messages only)
kafka-console-consumer --topic test-topic --bootstrap-server localhost:9092

# Explicitly specify latest
kafka-console-consumer --topic test-topic --bootstrap-server localhost:9092 --offset latest

# Consumer reads all messages from the beginning
kafka-console-consumer --topic test-topic --bootstrap-server localhost:9092 --from-beginning

# Explicitly specify earliest
kafka-console-consumer --topic test-topic --bootstrap-server localhost:9092 --offset earliest

# Reset consumer group offset to earliest
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group --reset-offsets --to-earliest --topic test-topic --execute

# Reset consumer group offset to latest
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group --reset-offsets --to-latest --topic test-topic --execute

# Reset to specific offset
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group --reset-offsets --to-offset 100 --topic test-topic --execute

# List all consumer groups
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Describe a consumer group (shows current offsets)
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group --describe

# Delete a consumer group
kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group --delete

# Check if all topics are deleted
kafka-topics --bootstrap-server localhost:9092 --list

# Check consumer group status
kafka-consumer-groups --bootstrap-server localhost:9092 --list

# Check topic details (partitions, replicas, etc.)
kafka-topics --bootstrap-server localhost:9092 --describe --topic test-topic

```

__Command to Describe a Topic__

```sh
bin/kafka-topics.sh --describe --topic messages --bootstrap-server localhost:9092
```

__Command to producing messages to the topic__

```sh
bin/kafka-console-producer.sh --topic messages --bootstrap-server localhost:9092
```

__Command to consuming messages from a topic__

```sh
bin/kafka-console-consumer.sh --topic messages --bootstrap-server localhost:9092
```

__Command to consume all the messages from the beginning__

```sh
bin/kafka-console-consumer.sh --topic messages --from-beginning --bootstrap-server localhost:9092
```

__Command to create a Kafka topic named “sample-topic” with 1 partition and a replication factor of 1__

```sh
bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic sample-topic
```

__Command to stop Kafka server__

```sh
bin/kafka-server-stop.sh
```
