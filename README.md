# RakNetty

RakNetty is a clean and modern implementation (port) of Oculus's [RakNet](https://github.com/facebookarchive/RakNet) in java.

## Why RakNetty?

RakNet is a networking library used by many project, including the renowned Minecraft: Bedrock Edition and Unity Engine.
RakNet uses UDP as its networking protocol, which is connectionless by default and packets are not guaranteed to reach
the destination, in comparison with TCP. RakNet implements a number of algorithms to ensure that packets can be
delivered in an ordered and reliable way. **RakNetty** ports the protocol and algorithms to java.

**RakNetty** is built based on Netty, a high-performance asynchronous event-driven framework. In comparison with the
original c++ version using Blocking IO, **RakNetty** has the advantages of Non-blocking IO to further improve the overall
performance, which is achieved by dispatching the packets to be handled by multiple threads. In comparison, the original version of RakNet also handles the packets for all connections in a
single thread.

## Current status

Currently, RakNetty is functional and supports most RakNet features, but only trivially tested. The codes are also organised in a Netty-like structure
with Java style conversion rather than a byte-to-byte copy of the original c++ version.

## Usage

### Maven repository

```xml
<repositories>
    <repository>
        <id>nukkit-releases</id>
        <url>https://nukkit.org/nexus/repository/maven-releases/</url>
    </repository>
</repositories>
```

### Dependency

```xml
<dependency>
    <groupId>org.nukkit</groupId>
    <artifactId>raknetty</artifactId>
    <version>1.0</version>
</dependency>
```

### Create a client

See [Example Client](https://github.com/NukkitReborn/RakNetty/blob/master/src/main/java/org/nukkit/raknetty/example/RakNettyClient.java)

### Create a server

See [Example Server](https://github.com/NukkitReborn/RakNetty/blob/master/src/main/java/org/nukkit/raknetty/example/RakNettyServer.java)

## Channel Options

RakNet defines a number of constants in its original code, which allows the developers to override them by redefining.
Minecraft: Bedrock Edition uses a different version of RakNet and changes some constants. For purpose of general use,
RakNetty supports the override of constants in a different way, by making advantage of Netty's Channel Options.

### Server options

| Option                          | Description                                | Default RakNet | Bedrock       |
| ------------------------------- | ------------------------------------------ | -------------- | ------------- |
| RAKNET_GUID                     | Guid of the server                         | Random         | Random        |
| RAKNET_NUMBER_OF_INTERNAL_IDS   | Size of address list in connection request | 10             | 20            |
| RAKNET_PROTOCOL_VERSION         | Version of RakNet protocol                 | 6              | 10            |
| RAKNET_MAX_CONNECTIONS          | Number of maximum connections              | User-specific  | User-specific |
| RAKNET_MAX_MTU_SIZE             | Maximum allowable MTU size                 | 1492           | 1400          |
| RAKNET_OFFLINE_RESPONSE         | Offline response when pinging              | String         | Server MOTD   |

Usage:

```java
ServerBootstrap boot; // your own server bootstrap

boot.option(RakServerChannelOption.RAKNET_GUID, 123456L)
    .option(RakServerChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
    .option(RakServerChannelOption.RAKNET_PROTOCOL_VERSION, 10)
    .option(RakServerChannelOption.RAKNET_MAX_CONNECTIONS, 15)
    .option(RakServerChannelOption.RAKNET_MAX_MTU_SIZE, 1400)
    .option(RakServerChannelOption.RAKNET_OFFLINE_RESPONSE, new ExampleBedrockPingResponse());
```

### Client options

| Option                        | Description                                              | Default RakNet  | Bedrock         |
| ----------------------------- | -------------------------------------------------------- | --------------- | --------------- |
| RAKNET_GUID                   | Guid of the server                                       | Random          | Random          |
| RAKNET_NUMBER_OF_INTERNAL_IDS | Size of address list in connection request               | 10              | 20              |
| RAKNET_PROTOCOL_VERSION       | Version of RakNet protocol                               | 6               | 10              |
| RAKNET_CONNECT_MTU_SIZES      | Sizes for trial to detect the MTU size                   | 1492, 1200, 576 | 1492, 1200, 576 |
| RAKNET_CONNECT_ATTEMPTS       | Attempts to be made before the connection request failed | 6               | 12              |
| RAKNET_CONNECT_INTERVAL       | Interval between each connection request                 | 1000            | 500             |
| RAKNET_CONNECT_TIMEOUT        | Timeout of connection request                            | 0               | 0               |
| RAKNET_UNRELIABLE_TIMEOUT     | Timeout of unreliable packets to be discarded            | 0               | 0               |
| RAKNET_TIMEOUT                | Timeout of connection                                    | 10000           | 10000           |

Usage:

```java
Bootstrap boot; // your own bootstrap
boot.option(RakChannelOption.RAKNET_GUID, 654321L)
    .option(RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
    .option(RakChannelOption.RAKNET_PROTOCOL_VERSION, 10)
    .option(RakChannelOption.RAKNET_CONNECT_INTERVAL, 500)
    .option(RakChannelOption.RAKNET_CONNECT_ATTEMPTS, 12);
```