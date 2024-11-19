# Start Zookeeper

__Command to start the zookeeper__

_bin/zookeeper-server-start.sh config/zookeeper.properties_

# Start Kafka server

__Command to start Kafka server__

_bin/kafka-server-start.sh config/server.properties_

__Command to know kafka version__ 

_bin/kafka-broker-api-versions.sh --version_

__Command to Create Topic__ 

_bin/kafka-topics.sh --create --topic messages --bootstrap-server localhost:9092_

__Command to List all the Topics__

_bin/kafka-topics.sh --list --bootstrap-server localhost:9092_

__Command to Describe a Topic__

_bin/kafka-topics.sh --describe --topic messages --bootstrap-server localhost:9092_

__Command to producing messages to the topic__

_bin/kafka-console-producer.sh --topic messages --bootstrap-server localhost:9092_

__Command to consuming messages from a topic__

_bin/kafka-console-consumer.sh --topic messages --bootstrap-server localhost:9092_

__Command to consume all the messages from the beginning__

_bin/kafka-console-consumer.sh --topic messages --from-beginning --bootstrap-server localhost:9092_

__Command to create a Kafka topic named “sample-topic” with 1 partition and a replication factor of 1__

_bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic sample-topic_

__Command to stop Kafka server__

_bin/kafka-server-stop.sh_
