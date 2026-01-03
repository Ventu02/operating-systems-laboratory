# Operating Systems Lab. Project

This repository contains the **Operating Systems Laboratory** project developed by **Group GVB**. The project consists of a client-server application implementing a topic-based messaging platform using **Java** sockets.

## Project Description

The application is based on a **client-server architecture** where multiple clients collaborate with a central server to manage messaging via specific topics.

The architecture is designed to be modular and supports:
* **Concurrent communication** between multiple clients using TCP sockets.
* **Centralized management** of topics and messages.
* **Interactive server sessions** for deep inspection and management of topics.

When a client connects, the server assigns a dedicated thread (`ClientHandler`) to manage its requests independently. Access to shared resources (topics, subscribers, messages) is synchronized to prevent race conditions.

## Features

### Roles and Functionality
Clients can assume different roles based on their actions:
* **Publisher**: Can create topics and publish messages.
* **Subscriber**: Can subscribe to topics to receive updates.
* **Guest**: Can view available topics.

### Concurrency Management
* **Thread-Safety**: The server uses `CopyOnWriteArrayList` to manage the list of connected clients safely during concurrent read/write operations.
* **Synchronization**: `synchronized` blocks are used for critical sections accessing shared data (TopicManager, SubscriberManager) to ensure data consistency.
* **Interactive Sessions**: The server can initiate an exclusive "inspection" mode on a topic, blocking concurrent commands on that specific topic until the session ends.

## Commands

### Client Commands
* `publish <topic>`: Registers the client as a publisher for the specified topic (creating it if it doesn't exist).
* `subscribe <topic>`: Registers the client as a subscriber for the specified topic.
* `send <message>`: Sends a message to the current topic (Publisher only).
* `list`: Shows messages sent by the current publisher (Publisher/Subscriber only).
* `listall`: Shows all messages in the current topic (Publisher/Subscriber only).
* `show`: Lists all existing topics.
* `quit`: Closes the connection.

### Server Commands
* `show`: Lists all existing topics.
* `quit`: Disconnects all clients and shuts down the server.
* `inspect <topic>`: Starts an interactive session for the specified topic.
    * `:listall`: Lists all messages in the topic.
    * `:delete <id>`: Deletes a message by ID.
    * `:end`: Ends the interactive session and resumes normal operation.

## Requirements

* **JDK 17** or higher.
* No external libraries are required.

## How to Run

1.  **Compile and Run the Server**:
    Open a terminal in the source folder (`src`) and run:
    ```bash
    java Server.java
    ```

2.  **Compile and Run Clients**:
    Open one or more new terminals in the source folder and run:
    ```bash
    java Client.java
    ```

## Contributors - Group GVB

* **Riccardo Ventura**
* **Umberto Gottardi**
* **Matteo Biacchessi**
