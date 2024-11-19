# Start Zookeeper

Command to start the zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka server

Command to start Kafka server
bin/kafka-server-start.sh config/server.properties

Command to know kafka version 
bin/kafka-broker-api-versions.sh --version

Command to Create Topic 
bin/kafka-topics.sh --create --topic messages --bootstrap-server localhost:9092

Command to List all the Topics
bin/kafka-topics.sh --list --bootstrap-server localhost:9092

Command to Describe a Topic
bin/kafka-topics.sh --describe --topic messages --bootstrap-server localhost:9092

Command to producing messages to the topic
bin/kafka-console-producer.sh --topic messages --bootstrap-server localhost:9092

Command to consuming messages from a topic
bin/kafka-console-consumer.sh --topic messages --bootstrap-server localhost:9092

Command to consume all the messages from the beginning
bin/kafka-console-consumer.sh --topic messages --from-beginning --bootstrap-server localhost:9092

Command to create a Kafka topic named “sample-topic” with 1 partition and a replication factor of 1
kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic sample-topic

Command to stop Kafka server
bin/kafka-server-stop.sh
