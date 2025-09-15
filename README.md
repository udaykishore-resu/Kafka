# KAFKA SETUP locally

```sh
#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Kafka version and directories
KAFKA_VERSION="4.1.0"
SCALA_VERSION="2.13"
KAFKA_DIR="kafka_${SCALA_VERSION}-${KAFKA_VERSION}"
KAFKA_ARCHIVE="${KAFKA_DIR}.tgz"
KAFKA_URL="https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_ARCHIVE"
KAFKA_HOME="$HOME/kafka"
KAFKA_INSTALL_DIR="$KAFKA_HOME/$KAFKA_DIR"
KRAFT_DIR="$KAFKA_INSTALL_DIR/config/kraft"
SERVER_PROPS="$KRAFT_DIR/server.properties"

echo -e "${BLUE}=== Kafka Setup Script ===${NC}"

# Function to check if Kafka is already installed
check_kafka_installation(){
    if command -v kafka-topics &>/dev/null; then
        echo -e "${GREEN}✓ Kafka is already installed${NC}"
        KAFKA_VER=$(kafka-topics --version 2>/dev/null | head -n1)
        echo -e "${BLUE}Kafka version detected:${NC} ${YELLOW}$KAFKA_VER${NC}"
        return 0
    else
        echo -e "${YELLOW}Kafka not found in PATH. Proceeding with download...${NC}"
        return 1
    fi
}

# Function to verify installation
verify_installation() {
    echo -e "${BLUE}Verifying Kafka installation...${NC}"
    
    cd "$KAFKA_INSTALL_DIR"
    
    # Check key files
    local key_files=(
        "bin/kafka-server-start.sh"
        "bin/kafka-topics.sh"
        "bin/kafka-console-producer.sh"
        "bin/kafka-console-consumer.sh"
        "bin/kafka-storage.sh"
        "config/kraft/server.properties"
    )
    
    for file in "${key_files[@]}"; do
        if [ -f "$file" ]; then
            echo -e "${GREEN}✓ $file${NC}"
        else
            echo -e "${RED}✗ $file (missing)${NC}"
        fi
    done
    
    # Test kafka-topics command
    if bin/kafka-topics.sh --version >/dev/null 2>&1; then
        KAFKA_VERSION_OUTPUT=$(bin/kafka-topics.sh --version 2>/dev/null | head -1)
        echo -e "${GREEN}✓ Kafka commands working: $KAFKA_VERSION_OUTPUT${NC}"
    else
        echo -e "${YELLOW}⚠ Kafka commands may need Java to be installed${NC}"
    fi
}

# Function to show next steps
show_next_steps() {
    echo -e "\n${GREEN}=== Kafka Installation Complete! ===${NC}"
    echo -e "${BLUE}Installation Directory:${NC} $KAFKA_INSTALL_DIR"
    echo -e "\n${YELLOW}Next Steps:${NC}"
    echo -e "1. Navigate to Kafka directory:"
    echo -e "   ${BLUE}cd $KAFKA_INSTALL_DIR${NC}"
    echo -e "\n2. Create KRaft configuration:"
    echo -e "   ${BLUE}mkdir -p config/kraft${NC}"
    echo -e "\n3. Generate cluster UUID and format storage:"
    echo -e "   ${BLUE}KAFKA_CLUSTER_ID=\$(bin/kafka-storage.sh random-uuid)${NC}"
    echo -e "   ${BLUE}bin/kafka-storage.sh format -t \$KAFKA_CLUSTER_ID -c config/kraft/server.properties --standalone${NC}"
    echo -e "\n4. Start Kafka:"
    echo -e "   ${BLUE}bin/kafka-server-start.sh config/kraft/server.properties${NC}"
    echo -e "\n${YELLOW}Or use the full path commands:${NC}"
    echo -e "   ${BLUE}$KAFKA_INSTALL_DIR/bin/kafka-server-start.sh${NC}"
}

# Function to create kafka directory
create_kafka_directory() {
    if [ ! -d "$KAFKA_HOME" ]; then
        echo -e "${BLUE}Creating Kafka home directory: $KAFKA_HOME${NC}"
        mkdir -p "$KAFKA_HOME"
    else
        echo -e "${GREEN}✓ Kafka home directory exists: $KAFKA_HOME${NC}"
    fi
}

# Function to download Kafka
download_kafka() {
    echo -e "${BLUE}Changing to Kafka directory: $KAFKA_HOME${NC}"
    cd "$KAFKA_HOME"
    
    # Check if archive already exists
    if [ -f "$KAFKA_ARCHIVE" ]; then
        echo -e "${YELLOW}Archive $KAFKA_ARCHIVE already exists. Checking integrity...${NC}"
        
        # Basic check - if archive is very small, it's probably corrupted
        ARCHIVE_SIZE=$(stat -f%z "$KAFKA_ARCHIVE" 2>/dev/null || stat -c%s "$KAFKA_ARCHIVE" 2>/dev/null)
        if [ "$ARCHIVE_SIZE" -lt 50000000 ]; then # Less than 50MB is suspicious
            echo -e "${YELLOW}Archive seems corrupted (size: $ARCHIVE_SIZE bytes). Re-downloading...${NC}"
            rm -f "$KAFKA_ARCHIVE"
        else
            echo -e "${GREEN}✓ Archive exists and seems valid${NC}"
        fi
    fi
    
    # Download if not exists or corrupted
    if [ ! -f "$KAFKA_ARCHIVE" ]; then
        echo -e "${BLUE}Downloading Kafka $KAFKA_VERSION...${NC}"
        echo -e "${BLUE}URL: https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_ARCHIVE${NC}"
        
        if curl -f -O "https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_ARCHIVE"; then
            echo -e "${GREEN}✓ Download completed successfully${NC}"
        else
            echo -e "${RED}✗ Download failed. Please check your internet connection or try a different mirror.${NC}"
            echo -e "${YELLOW}Alternative download URLs:${NC}"
            echo -e "  https://archive.apache.org/dist/kafka/$KAFKA_VERSION/$KAFKA_ARCHIVE"
            echo -e "  https://dlcdn.apache.org/kafka/$KAFKA_VERSION/$KAFKA_ARCHIVE"
            exit 1
        fi
    fi
}

# Function to extract Kafka
extract_kafka() {
    cd "$KAFKA_HOME"
    
    if [ -d "$KAFKA_DIR" ]; then
        echo -e "${YELLOW}Kafka directory exists. Removing old installation...${NC}"
        rm -rf "$KAFKA_DIR"
    fi
    
    echo -e "${BLUE}Extracting Kafka archive...${NC}"
    if tar -xzf "$KAFKA_ARCHIVE"; then
        echo -e "${GREEN}✓ Extraction completed successfully${NC}"
        
        # Verify extraction
        if [ -d "$KAFKA_DIR" ] && [ -f "$KAFKA_DIR/bin/kafka-server-start.sh" ]; then
            echo -e "${GREEN}✓ Kafka installation verified${NC}"
        else
            echo -e "${RED}✗ Extraction failed or files are missing${NC}"
            exit 1
        fi
    else
        echo -e "${RED}✗ Failed to extract Kafka archive${NC}"
        exit 1
    fi
}

# Function to set up environment
setup_environment() {
    echo -e "${BLUE}Setting up Kafka environment...${NC}"
    
    # Add Kafka bin to PATH for current session
    export PATH="$KAFKA_INSTALL_DIR/bin:$PATH"
    
    # Check if already added to shell profile
    SHELL_PROFILE=""
    if [ -f "$HOME/.zshrc" ]; then
        SHELL_PROFILE="$HOME/.zshrc"
    elif [ -f "$HOME/.bash_profile" ]; then
        SHELL_PROFILE="$HOME/.bash_profile"
    elif [ -f "$HOME/.bashrc" ]; then
        SHELL_PROFILE="$HOME/.bashrc"
    fi
    
    if [ -n "$SHELL_PROFILE" ]; then
        KAFKA_PATH_LINE="export PATH=\"$KAFKA_INSTALL_DIR/bin:\$PATH\""
        
        if ! grep -q "$KAFKA_INSTALL_DIR/bin" "$SHELL_PROFILE"; then
            echo -e "${BLUE}Adding Kafka to PATH in $SHELL_PROFILE${NC}"
            echo "" >> "$SHELL_PROFILE"
            echo "# Kafka" >> "$SHELL_PROFILE"
            echo "$KAFKA_PATH_LINE" >> "$SHELL_PROFILE"
            echo -e "${GREEN}✓ Added Kafka to PATH. Run 'source $SHELL_PROFILE' or restart your terminal${NC}"
        else
            echo -e "${GREEN}✓ Kafka already in PATH${NC}"
        fi
    fi
    
    echo -e "${BLUE}Current Kafka installation directory: $KAFKA_INSTALL_DIR${NC}"
}

locate_kafka_home() {
    echo -e "${GREEN}✓ locate_kafka_home entry${NC}"
    if [ -d "$KAFKA_INSTALL_DIR" ]; then
        export KAFKA_HOME="$KAFKA_INSTALL_DIR"
        echo -e "${GREEN}✓ Kafka home detected at: $KAFKA_HOME${NC}"
    elif command -v kafka-topics >/dev/null 2>&1; then
        # Try to detect from PATH
        KAFKA_BIN=$(command -v kafka-topics)
        export KAFKA_HOME="$(dirname "$(dirname "$KAFKA_BIN")")"
        echo -e "${GREEN}✓ Kafka home detected at: $KAFKA_HOME${NC}"
    else
        echo -e "${RED}✗ Failed to locate Kafka home${NC}"
        return 1
    fi
    echo -e "${GREEN}✓ locate_kafka_home exit${NC}"
}

change_dir_kafka_home(){
    echo -e "${GREEN}✓ change_dir_kafka_home entry${NC}"
    if [ -d "$KAFKA_INSTALL_DIR" ]; then
        cd "$KAFKA_HOME" || exit 1
        echo -e "${BLUE}Now inside Kafka home: ${NC}$PWD"
    else
        echo -e "${RED}✗ Failed to locate Kafka home${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ change_dir_kafka_home exit${NC}"
}

# Check ports 9092 & 9093 and free them
cleanup_kafka_ports() {
    echo "Cleaning Kafka ports 9092 & 9093..."

    # Kill all Kafka Java processes
    sudo pkill -f 'kafka.Kafka'

    # Ensure ports are free
    for port in 9092 9093; do
        while sudo lsof -t -i:$port >/dev/null; do
            echo "Force killing processes on port $port..."
            sudo lsof -t -i:$port | xargs -r sudo kill -9
            sleep 1
        done
        echo "Port $port is free."
    done
    echo "Kafka ports are fully free."
}


check_kraft_config(){
    # Check & create KRaft config directory
    if [ ! -d "$KRAFT_DIR" ]; then
        echo -e "${YELLOW}KRaft config directory not found. Creating...${NC}"
        mkdir -p "$KRAFT_DIR"
        echo -e "${GREEN}✓ Created directory: $KRAFT_DIR${NC}"
    else
        echo -e "${GREEN}✓ KRaft config directory already exists${NC}"
    fi

    # Check & create server.properties
    if [ ! -f "$SERVER_PROPS" ]; then
        echo -e "${YELLOW}server.properties not found. Creating...${NC}"
        cat > "$SERVER_PROPS" << 'EOF'
        # Licensed to the Apache Software Foundation (ASF) under one or more
        # contributor license agreements.  See the NOTICE file distributed with
        # this work for additional information regarding copyright ownership.
        # The ASF licenses this file to You under the Apache License, Version 2.0
        # (the "License"); you may not use this file except in compliance with
        # the License.  You may obtain a copy of the License at
        #
        #    http://www.apache.org/licenses/LICENSE-2.0
        #
        # Unless required by applicable law or agreed to in writing, software
        # distributed under the License is distributed on an "AS IS" BASIS,
        # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        # See the License for the specific language governing permissions and
        # limitations under the License.

        ############################# KRaft Mode Configuration #############################

        # The role of this server. Setting this puts us in KRaft mode
        process.roles=broker,controller

        # The node id associated with this instance's roles
        node.id=1

        # The connect string for the controller quorum
        controller.quorum.voters=1@localhost:9093

        ############################# Socket Server Settings #############################

        # The address the socket server listens on
        listeners=PLAINTEXT://:9092,CONTROLLER://:9094

        # Listener name, hostname and port the broker will advertise to clients
        advertised.listeners=PLAINTEXT://localhost:9092

        # Maps listener names to security protocols
        listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL

        # Name of listener used for communication between brokers
        inter.broker.listener.name=PLAINTEXT

        # Listener name used by the controller
        controller.listener.names=CONTROLLER

        # The number of threads that the server uses for receiving requests from the network and sending responses to the network
        num.network.threads=3

        # The number of threads that the server uses for processing requests, which may include disk I/O
        num.io.threads=8

        # The send buffer (SO_SNDBUF) used by the socket server
        socket.send.buffer.bytes=102400

        # The receive buffer (SO_RCVBUF) used by the socket server
        socket.receive.buffer.bytes=102400

        # The maximum size of a request that the socket server will accept (protection against OOM)
        socket.request.max.bytes=104857600

        ############################# Log Basics #############################

        # A comma separated list of directories under which to store log files
        log.dirs=/tmp/kraft-combined-logs

        # The default number of log partitions per topic
        num.partitions=1

        # The number of threads per data directory to be used for log recovery at startup and flushing at shutdown
        num.recovery.threads.per.data.dir=1

        ############################# Internal Topic Settings  #############################
        # The replication factor for the group metadata internal topics "__consumer_offsets" and "__transaction_state"
        offsets.topic.replication.factor=1
        transaction.state.log.replication.factor=1
        transaction.state.log.min.isr=1

        ############################# Log Retention Policy #############################

        # The minimum age of a log file to be eligible for deletion due to age
        log.retention.hours=168

        # The maximum size of a log segment file. When this size is reached a new log segment will be created
        log.segment.bytes=1073741824

        # The interval at which log segments are checked to see if they can be deleted according to the retention policies
        log.retention.check.interval.ms=300000
EOF
    fi
    echo -e "${GREEN}✓ KRaft configuration created${NC}"

}

# Clean up old logs and format storage
cleanup_oldlogs(){
    echo -e "${BLUE}Cleaning up old logs and formatting storage...${NC}"
    rm -rf /tmp/kraft-combined-logs
}

# Generate cluster UUID
generate_cluster_id(){
    KAFKA_CLUSTER_ID=$(bin/kafka-storage.sh random-uuid)
    echo -e "${GREEN}Generated Cluster ID: $KAFKA_CLUSTER_ID${NC}"
}

# Format storage
format_storage(){
    echo -e "${BLUE}Formatting Kafka storage...${NC}"
    bin/kafka-storage.sh format -t $KAFKA_CLUSTER_ID -c config/kraft/server.properties --standalone

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Storage formatted successfully${NC}"
    else
        echo -e "${RED}✗ Storage formatting failed${NC}"
        exit 1
    fi
}

# Function to show post-installation instructions
show_instructions() {
    echo -e "\n${GREEN}=== Kafka Setup Complete! ===${NC}"
    echo -e "${BLUE}Current directory: $(pwd)${NC}"
    echo -e "\n${YELLOW}=== Next Steps ===${NC}"
    echo -e "${BLUE}1. Start Kafka Server:${NC}"
    echo -e "   bin/kafka-server-start.sh config/kraft/server.properties"
    echo -e "\n${BLUE}2. Open a new terminal tab/window and create a topic:${NC}"
    echo -e "   cd $(pwd)"
    echo -e "   bin/kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092"
    echo -e "\n${BLUE}3. Open another new terminal tab/window and start a producer:${NC}"
    echo -e "   cd $(pwd)"
    echo -e "   bin/kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092"
    echo -e "\n${BLUE}4. Open another new terminal tab/window and start a consumer:${NC}"
    echo -e "   cd $(pwd)"
    echo -e "   bin/kafka-console-consumer.sh --topic test-topic --from-beginning --bootstrap-server localhost:9092"
    echo -e "\n${YELLOW}=== Useful Commands ===${NC}"
    echo -e "${BLUE}List topics:${NC}"
    echo -e "   bin/kafka-topics.sh --list --bootstrap-server localhost:9092"
    echo -e "\n${BLUE}Delete a topic:${NC}"
    echo -e "   bin/kafka-topics.sh --delete --topic topic-name --bootstrap-server localhost:9092"
    echo -e "\n${BLUE}List consumer groups:${NC}"
    echo -e "   bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9092"
    echo -e "\n${BLUE}Stop Kafka:${NC}"
    echo -e "   Press Ctrl+C in the server terminal"
}

# Main execution
main(){
    echo -e "${BLUE}Starting Kafka installation process...${NC}"
    echo -e "${BLUE}Checking for existing Kafka installation...${NC}"

    if check_kafka_installation; then
        echo -e "${GREEN}Kafka is already set up!${NC}"
        verify_installation
        show_next_steps
        # exit 0
    else
        # Create directory structure
        create_kafka_directory

        # Download Kafka
        download_kafka
        
        # Extract Kafka
        extract_kafka

        # Set up environment
        setup_environment

        # Verify installation
        verify_installation
        
        # Show next steps
        show_next_steps
    fi

    echo -e "\n${GREEN}Installation completed successfully!${NC}"

    # Locate Kafka home
    locate_kafka_home

    # Change directory to Kafka home
    change_dir_kafka_home
    sleep 1

    # Check ports 9092 & 9093 and free them
    cleanup_kafka_ports
    sleep 3

    # Create kraft config directory (if it doesn't exist)
    check_kraft_config

    # Clean up old logs and format storage
    cleanup_oldlogs

    # Generate cluster UUID
    generate_cluster_id

    # Format storage
    format_storage

    # Ask user if they want to start Kafka now or just show instructions
    echo -e "\n${YELLOW}Do you want to start Kafka server now? (y/n)${NC}"
    read -r START_NOW

    if [[ $START_NOW =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Starting Kafka server...${NC}"
        echo -e "${YELLOW}Note: This will run in the foreground. Use Ctrl+C to stop.${NC}"
        echo -e "${YELLOW}To run in background, use: nohup bin/kafka-server-start.sh config/kraft/server.properties > kafka.log 2>&1 &${NC}"
        sleep 2
        
        # Start Kafka server
        bin/kafka-server-start.sh config/kraft/server.properties
    else
        show_instructions
    fi

}

# Check if Java is installed
check_java() {
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -1)
        echo -e "${GREEN}✓ Java is installed: $JAVA_VERSION${NC}"
    else
        echo -e "${YELLOW}⚠ Java not found. Kafka requires Java 11 or higher.${NC}"
        echo -e "${YELLOW}Install Java with: brew install openjdk@17${NC}"
    fi
}

# Run checks and main function
echo -e "${BLUE}Checking prerequisites...${NC}"
check_java
echo ""

main

```

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
