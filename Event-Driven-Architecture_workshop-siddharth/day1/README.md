# Event-Driven Architecture E-commerce Order Processing System

**Technology Stack:** Scala, Akka, RabbitMQ  
**Architecture Pattern:** Event-Driven Architecture (EDA)

## Table of Contents

1. [Project Overview](#project-overview)
2. [What is an Event in Event-Driven Architecture?](#what-is-an-event-in-event-driven-architecture)
3. [Event-Driven Architecture vs Request-Response Architecture](#event-driven-architecture-vs-request-response-architecture)
4. [EDA in E-commerce Applications](#eda-in-e-commerce-applications)
5. [EDA and Microservices/Cloud-Native Systems](#eda-and-microservicescloud-native-systems)
6. [Building Scalable Systems with EDA](#building-scalable-systems-with-eda)
7. [Implementation Details](#implementation-details)
8. [Getting Started](#getting-started)
9. [System Architecture](#system-architecture)
10. [References](#references)

## Project Overview

This project demonstrates the core components of Event-Driven Architecture (EDA) through a simplified e-commerce order processing system. The implementation showcases how producers generate events, message brokers route these events, and consumers process them asynchronously, embodying the principles of loose coupling and asynchronous communication that make EDA particularly suitable for modern distributed systems.

The system implements three fundamental components of EDA: a producer that generates "OrderPlaced" events when customers place orders, a RabbitMQ message broker that routes events to appropriate queues, and a consumer that processes these events by simulating order fulfillment operations. This architecture demonstrates how different parts of an e-commerce system can operate independently while maintaining coordination through event-based communication.

## What is an Event in Event-Driven Architecture?

An event in Event-Driven Architecture represents a significant occurrence or change in state within a system that other components might be interested in knowing about. Events are immutable records that capture what happened, when it happened, and relevant contextual information about the occurrence. They serve as the fundamental communication mechanism between different parts of a distributed system, enabling loose coupling and asynchronous processing.

### Real-World Example: Library System

Consider a traditional public library system as a real-world analogy for understanding events. When a patron returns a book, this action triggers several cascading activities throughout the library system, much like how events propagate through a software architecture.

The moment a book is returned represents an event - "BookReturned." This single occurrence contains important information: which book was returned, who returned it, when it was returned, and the condition of the book. Just as in Event-Driven Architecture, this event doesn't directly command other systems what to do; instead, it simply announces that something significant has happened.

Various library departments respond to this "BookReturned" event independently. The circulation desk updates their records to show the book is available for checkout again. The maintenance department checks if the book needs repair or cleaning. The reservation system notifies the next patron on the waiting list that their requested book is now available. The late fees department calculates any applicable charges. The inventory management system updates the book's location status.

Each of these departments operates independently and doesn't need to know about the others' existence or processes. They simply listen for "BookReturned" events and respond according to their specific responsibilities. If the library adds a new department, such as a digital cataloging system, it can start listening for these events without requiring changes to the book return process or other existing departments.

This independence and loose coupling mirror the benefits of Event-Driven Architecture in software systems. The book return process (producer) doesn't need to know about all the downstream processes (consumers). New processes can be added without modifying existing ones. Each department can process the event at its own pace and according to its own business rules.

### Event Characteristics in Software Systems

In software systems, events possess several key characteristics that distinguish them from other forms of communication. Events are immutable, meaning once created, they cannot be changed. This immutability ensures data consistency and enables reliable event replay for debugging or system recovery. Events are also timestamped, providing a chronological order of occurrences that can be crucial for understanding system behavior and maintaining data consistency across distributed components.

Events carry sufficient context to be meaningful to consumers without requiring additional queries. A well-designed event includes all the information necessary for consumers to make decisions and take appropriate actions. This self-contained nature reduces coupling between components and improves system resilience by minimizing dependencies on external data sources during event processing.

## Event-Driven Architecture vs Request-Response Architecture

The fundamental difference between Event-Driven Architecture and Request-Response Architecture lies in their communication patterns and coupling characteristics. Understanding these differences is crucial for making informed architectural decisions in modern software development.

### Request-Response Architecture

Request-Response Architecture follows a synchronous, tightly-coupled communication pattern where one component directly requests information or action from another component and waits for a response. This pattern is exemplified by traditional web applications where a client sends an HTTP request to a server and waits for the server's response before proceeding.

In Request-Response systems, the requesting component must know the specific location, interface, and availability of the target component. This creates tight coupling between components, as changes to one component often require corresponding changes to its clients. The synchronous nature means that the requesting component blocks until it receives a response, which can lead to cascading failures when downstream services become unavailable or slow.

Consider an e-commerce checkout process implemented with Request-Response architecture. When a customer places an order, the order service must synchronously call the inventory service to check stock levels, then call the payment service to process the payment, followed by calls to the shipping service to arrange delivery, and finally the notification service to send confirmation emails. Each step depends on the successful completion of the previous step, creating a chain of dependencies that can fail at any point.

### Event-Driven Architecture

Event-Driven Architecture, in contrast, employs asynchronous, loosely-coupled communication through events. Components publish events when significant state changes occur, and other components subscribe to events they're interested in processing. This decouples producers from consumers, as publishers don't need to know who will consume their events or how they will be processed.

The asynchronous nature of EDA allows components to continue processing without waiting for downstream operations to complete. This improves system responsiveness and resilience, as temporary failures in one component don't immediately impact others. Components can process events at their own pace, enabling better resource utilization and the ability to handle varying load patterns.

Using the same e-commerce example with EDA, when a customer places an order, the order service publishes an "OrderPlaced" event. Multiple services can independently subscribe to this event: the inventory service updates stock levels, the payment service processes the payment, the shipping service arranges delivery, and the notification service sends confirmation emails. Each service processes the event according to its own timeline and business rules, without blocking others.

### Advantages and Disadvantages Comparison

**Request-Response Architecture Advantages:**

Request-Response architecture offers simplicity and predictability that makes it suitable for many applications. The synchronous nature provides immediate feedback, making it easier to handle errors and maintain data consistency. Developers can follow a linear flow of execution, which simplifies debugging and testing. The tight coupling, while often seen as a disadvantage, can be beneficial in scenarios where strong consistency is required and the system components are closely related.

The request-response pattern aligns well with human mental models of cause and effect, making it intuitive for developers to understand and implement. It's particularly effective for simple CRUD operations, user interfaces that require immediate feedback, and systems where the number of components is small and well-defined.

**Request-Response Architecture Disadvantages:**

The primary disadvantages of Request-Response architecture stem from its synchronous and tightly-coupled nature. System availability becomes the product of all component availabilities, meaning that if any component in the chain fails, the entire operation fails. This creates cascading failure scenarios where problems in one service can bring down the entire system.

Performance is limited by the slowest component in the chain, as each request must wait for all downstream operations to complete. This can lead to poor user experience and inefficient resource utilization. Scaling becomes challenging because all components must be scaled together, and adding new functionality often requires modifying existing components.

The tight coupling makes the system brittle and difficult to evolve. Changes to one component's interface require coordinated updates across all its clients. This increases development complexity and slows down the pace of innovation, particularly in large organizations where different teams own different components.

**Event-Driven Architecture Advantages:**

Event-Driven Architecture excels in building resilient, scalable, and evolvable systems. The loose coupling between components allows them to evolve independently, enabling different teams to work on different parts of the system without constant coordination. New functionality can be added by simply subscribing to existing events, without modifying existing components.

The asynchronous nature improves system responsiveness and enables better resource utilization. Components can process events at their optimal pace, and temporary failures in one component don't immediately impact others. This resilience is particularly valuable in distributed systems where network partitions and service failures are inevitable.

EDA naturally supports horizontal scaling, as event processing can be distributed across multiple instances of consumer services. The event log provides a natural audit trail and enables powerful patterns like event sourcing and CQRS (Command Query Responsibility Segregation). The architecture also facilitates integration with external systems and supports complex business processes that span multiple services.

**Event-Driven Architecture Disadvantages:**

The primary challenges of Event-Driven Architecture relate to complexity and eventual consistency. The asynchronous nature makes it more difficult to reason about system behavior and debug issues. Developers must think in terms of eventual consistency rather than immediate consistency, which can be challenging for those accustomed to synchronous programming models.

Event ordering and duplicate handling become important considerations that must be explicitly addressed in the design. The system requires additional infrastructure components like message brokers, which introduce operational complexity and potential points of failure. Monitoring and observability become more challenging as operations span multiple services and occur asynchronously.

Testing can be more complex due to the asynchronous nature and the need to verify that events are properly published and consumed. The loose coupling, while beneficial for evolution, can make it harder to understand the complete system behavior and trace the flow of operations across services.

## EDA in E-commerce Applications

Event-Driven Architecture proves particularly valuable in e-commerce applications due to their inherently complex, multi-step business processes and the need for high availability and scalability. E-commerce systems must handle various interconnected operations while maintaining responsiveness and reliability for customers. Let's examine how EDA addresses specific e-commerce scenarios.

### Placing an Order

In an event-driven e-commerce system, the order placement process begins when a customer submits their order through the user interface. Instead of the order service directly calling multiple downstream services, it publishes an "OrderPlaced" event containing comprehensive order information including customer details, ordered items, quantities, pricing, and payment information.

This "OrderPlaced" event serves as the single source of truth about the order creation, and multiple services can subscribe to it independently. The inventory service listens for these events to reserve stock for the ordered items. The payment service processes the payment information. The fraud detection service analyzes the order for suspicious patterns. The recommendation service updates customer preferences based on purchase history. Each service processes the event according to its own business logic and timeline.

The event-driven approach provides several benefits for order placement. If the payment service is temporarily unavailable, the order can still be recorded, and payment processing can occur when the service recovers. This prevents lost sales due to temporary service outages. The system can also implement sophisticated retry mechanisms and dead letter queues to handle various failure scenarios gracefully.

Furthermore, the order placement process becomes highly extensible. New services can be added to handle additional aspects of order processing, such as loyalty point calculations, tax computations, or regulatory compliance checks, simply by subscribing to the "OrderPlaced" event without modifying the core order placement logic.

### Sending Confirmation Email

Email confirmation in an event-driven system demonstrates the power of loose coupling and asynchronous processing. Rather than the order service directly calling an email service, a dedicated notification service subscribes to "OrderPlaced" events and handles all customer communications.

When the notification service receives an "OrderPlaced" event, it can generate personalized confirmation emails based on the order details and customer preferences. The service can implement sophisticated logic for email templating, localization, and delivery timing without affecting the order placement process. If email delivery fails due to network issues or email service problems, the notification service can implement retry mechanisms and alternative communication channels.

The event-driven approach also enables rich communication workflows. The notification service can subscribe to multiple event types such as "OrderConfirmed," "OrderShipped," and "OrderDelivered" to send appropriate communications at each stage of the order lifecycle. This creates a comprehensive customer communication experience without tightly coupling the communication logic to the core business processes.

Additionally, the system can easily support multiple communication channels. A mobile push notification service can subscribe to the same events to send app notifications, while an SMS service can send text message updates. Each communication channel operates independently, allowing for different delivery guarantees and retry policies based on the channel characteristics.

### Updating Inventory

Inventory management in e-commerce systems requires careful coordination between multiple operations including order placement, cancellations, returns, and restocking. Event-Driven Architecture provides an elegant solution for maintaining accurate inventory levels while supporting complex business rules.

When an "OrderPlaced" event is published, the inventory service subscribes to this event and reserves the ordered quantities. This reservation prevents overselling while allowing for order cancellations before payment confirmation. The inventory service can implement sophisticated allocation algorithms, considering factors like warehouse locations, shipping costs, and item expiration dates.

The event-driven approach enables the inventory service to subscribe to various events throughout the order lifecycle. "OrderCancelled" events trigger the release of reserved inventory. "OrderReturned" events restore inventory levels and may trigger quality checks for returned items. "RestockReceived" events update available quantities when new inventory arrives at warehouses.

This architecture supports complex inventory scenarios such as backorder management, where the inventory service can publish "ItemBackordered" events when stock is insufficient. Other services can subscribe to these events to implement backorder notifications, supplier communications, and automatic reordering processes.

The inventory service can also implement advanced features like inventory forecasting by analyzing historical order patterns from the event stream. This enables proactive inventory management and reduces stockouts while minimizing carrying costs.

## EDA and Microservices/Cloud-Native Systems

Event-Driven Architecture and microservices represent complementary architectural patterns that together enable the construction of highly scalable, resilient, and maintainable distributed systems. The synergy between EDA and microservices addresses many of the fundamental challenges in building cloud-native applications.

### Reason 1: Natural Service Boundaries and Loose Coupling

Microservices architecture emphasizes decomposing applications into small, independently deployable services, each responsible for a specific business capability. However, one of the primary challenges in microservices is managing inter-service communication without creating tight coupling that undermines the benefits of service decomposition.

Event-Driven Architecture provides an ideal communication mechanism for microservices by establishing natural service boundaries based on business events rather than technical interfaces. Services communicate through well-defined events that represent business occurrences, rather than through direct API calls that create point-to-point dependencies.

This event-based communication enables services to evolve independently. When a service needs to change its internal implementation or data model, it only needs to maintain the contract of the events it publishes. Consuming services are unaffected by internal changes as long as the event structure remains compatible. This independence is crucial for organizations adopting microservices to enable different teams to work on different services without constant coordination.

The loose coupling provided by EDA also supports the microservices principle of failure isolation. When one service fails, it doesn't immediately cascade to other services because communication is asynchronous and mediated through the event infrastructure. Services can implement circuit breakers, bulkheads, and other resilience patterns more effectively when they're not directly dependent on synchronous calls to other services.

Furthermore, EDA enables services to subscribe only to the events they need, creating a natural form of interface segregation. This reduces the cognitive load on development teams and makes it easier to understand service dependencies. New services can be added to the system by subscribing to existing events without requiring changes to existing services, supporting the open-closed principle at the system architecture level.

### Reason 2: Scalability and Resource Optimization

Cloud-native systems must efficiently utilize cloud resources while providing elastic scalability to handle varying load patterns. Event-Driven Architecture enables fine-grained scalability by allowing different services to scale independently based on their specific load characteristics and processing requirements.

In an event-driven microservices system, each service can be scaled independently based on the volume and characteristics of events it processes. For example, in an e-commerce system, the order processing service might need to scale differently than the recommendation service or the notification service. EDA enables this independent scaling because services are decoupled through events rather than direct calls.

The asynchronous nature of event processing allows services to implement sophisticated load balancing and resource optimization strategies. Services can process events in batches to improve throughput, implement backpressure mechanisms to prevent overload, and use different processing strategies based on event priority or type. This flexibility is particularly valuable in cloud environments where resources can be dynamically allocated and deallocated based on demand.

Event-driven systems also support temporal decoupling, meaning that producers and consumers don't need to be available simultaneously. This enables more efficient resource utilization in cloud environments where services can be scaled down during low-demand periods and scaled up when event queues indicate increased load. The event infrastructure acts as a buffer that smooths out load spikes and enables more predictable resource planning.

The event log itself becomes a valuable resource for system optimization. By analyzing event patterns, organizations can identify bottlenecks, optimize service placement across cloud regions, and implement predictive scaling based on historical event volumes. This data-driven approach to system optimization is much more difficult to achieve with synchronous, request-response architectures.

Cloud-native platforms like Kubernetes provide excellent support for event-driven microservices through features like horizontal pod autoscaling based on queue depth, service mesh integration for observability, and native support for event-driven scaling through KEDA (Kubernetes Event-Driven Autoscaling). These platform capabilities, combined with EDA principles, enable the construction of truly elastic, cloud-native applications.

## Building Scalable Systems with EDA

Event-Driven Architecture provides fundamental capabilities for building scalable systems by addressing the key challenges of distributed computing: coordination, consistency, and performance. The architecture's inherent characteristics enable both horizontal and vertical scaling while maintaining system coherence and reliability.

### Scalability Mechanisms in EDA

The scalability of event-driven systems stems from several architectural properties that work together to enable efficient resource utilization and performance optimization. The asynchronous nature of event processing allows systems to decouple temporal dependencies, meaning that different parts of the system can operate at their optimal pace without being constrained by the slowest component.

Event partitioning enables horizontal scaling by distributing event processing across multiple instances of consumer services. Events can be partitioned based on various criteria such as customer ID, geographic region, or event type, allowing different partitions to be processed independently and in parallel. This partitioning strategy enables linear scalability where adding more processing instances directly increases system throughput.

The event log serves as a natural load balancer and buffer, smoothing out traffic spikes and enabling more predictable resource planning. During high-load periods, events accumulate in the log and are processed as resources become available. This buffering capability prevents system overload and enables graceful degradation under extreme load conditions.

Event-driven systems also support multiple consumption patterns that optimize for different scalability requirements. Competing consumers can process events from the same stream in parallel, while event broadcasting enables multiple services to process the same events independently. These patterns can be combined to create sophisticated processing topologies that optimize for both throughput and latency.

### Real-World Use Case 1: Global Social Media Platform

Consider a global social media platform that must handle billions of user interactions daily while providing real-time updates to users worldwide. Traditional monolithic architectures struggle with this scale due to the complexity of coordinating updates across multiple features and the need to maintain consistency across geographically distributed data centers.

An event-driven architecture transforms this challenge by treating each user action as an event that can be processed independently by multiple services. When a user posts content, a "ContentPosted" event is published containing the post details, user information, and metadata. This single event triggers multiple independent processing workflows that operate at different scales and with different performance requirements.

The content moderation service processes these events to detect inappropriate content using machine learning models that require significant computational resources. The recommendation service analyzes posting patterns to update user interest profiles and content recommendations. The notification service determines which users should be notified about the new content based on their social connections and preferences. The analytics service aggregates posting data for business intelligence and trend analysis.

Each of these services can be scaled independently based on their specific requirements. The content moderation service might require GPU-accelerated instances for machine learning inference, while the notification service might need high-throughput message processing capabilities. The analytics service might process events in large batches for efficiency, while the recommendation service might prioritize low-latency processing for real-time updates.

The event-driven architecture enables the platform to handle traffic spikes gracefully. During major events or viral content scenarios, the event infrastructure buffers the increased load while services scale up to handle the additional processing requirements. The asynchronous nature means that temporary delays in one service don't impact others, maintaining overall system responsiveness.

Geographic distribution becomes manageable through event replication strategies. Events can be replicated across multiple regions, allowing local processing while maintaining global consistency through eventual consistency patterns. This approach provides better user experience through reduced latency while maintaining the ability to provide globally consistent features like friend recommendations and content discovery.

The platform can also implement sophisticated features like A/B testing and feature rollouts through event-driven mechanisms. New features can be deployed as additional event consumers without affecting existing functionality. Feature flags can be implemented through event filtering, allowing gradual rollouts and easy rollbacks if issues are detected.

### Real-World Use Case 2: Financial Trading System

Financial trading systems represent one of the most demanding scalability challenges in software engineering, requiring ultra-low latency, high throughput, and absolute reliability while processing millions of transactions per second. Traditional monolithic architectures struggle with these requirements due to the complexity of coordinating multiple trading algorithms, risk management systems, and regulatory compliance processes.

An event-driven trading system treats each market data update, trade order, and execution as events that flow through a high-performance event processing pipeline. Market data events stream continuously from multiple exchanges and data providers, creating a real-time view of market conditions. Trade order events represent buy and sell requests from clients, while execution events record completed transactions.

The architecture enables multiple trading algorithms to operate independently while sharing the same market data stream. Each algorithm can subscribe to relevant market events and publish trade orders based on its specific strategy. This independence allows different algorithms to be developed, tested, and deployed by different teams without interfering with each other's operations.

Risk management becomes a cross-cutting concern that subscribes to all trading events to monitor positions, calculate exposures, and enforce trading limits in real-time. The risk management service can process events at extremely high throughput while maintaining low-latency responses for risk violations. When risk limits are exceeded, the service can publish "RiskViolation" events that trigger automatic position adjustments or trading halts.

The event-driven architecture enables sophisticated performance optimizations that are crucial for trading systems. Events can be processed using specialized hardware like FPGAs (Field-Programmable Gate Arrays) for ultra-low latency requirements. Different event types can be routed through different processing paths optimized for their specific characteristics - market data events might prioritize throughput while order events prioritize latency.

Regulatory compliance becomes manageable through comprehensive event logging and audit trails. Every trading decision, market data update, and system action is recorded as an immutable event, providing complete traceability for regulatory reporting and investigation purposes. The event log serves as the authoritative record of all system activity, enabling sophisticated compliance monitoring and reporting capabilities.

The system can implement complex trading strategies that span multiple markets and time horizons through event correlation and pattern matching. Long-term investment algorithms can subscribe to daily market summary events, while high-frequency trading algorithms process individual tick data events. This flexibility enables the platform to support diverse trading strategies within a unified architecture.

Disaster recovery and business continuity are enhanced through event replication and replay capabilities. The complete system state can be reconstructed from the event log, enabling rapid recovery from failures. Hot standby systems can process the same event stream in parallel, providing instant failover capabilities with minimal data loss.

The architecture also supports sophisticated testing and simulation capabilities. Historical event streams can be replayed to test new algorithms or validate system changes under realistic market conditions. This capability is crucial for financial systems where errors can result in significant financial losses.




## Implementation Details

This project implements a complete Event-Driven Architecture using Scala, Akka, and RabbitMQ to demonstrate the core concepts of event-driven systems in a practical e-commerce context. The implementation showcases best practices for building resilient, scalable, and maintainable event-driven applications.

### Technology Stack

**Scala 2.13.6** serves as the primary programming language, providing functional programming capabilities that align well with event-driven patterns. Scala's immutable data structures and pattern matching make it ideal for representing and processing events safely and efficiently.

**Akka 2.6.15** provides the actor-based concurrency model that handles event production and consumption. Akka actors offer lightweight, isolated units of computation that can process events asynchronously while maintaining thread safety and fault tolerance. The actor model naturally aligns with event-driven principles by treating each actor as an independent event processor.

**RabbitMQ** serves as the message broker, providing reliable event routing, persistence, and delivery guarantees. RabbitMQ's support for various exchange types, routing patterns, and quality-of-service configurations makes it suitable for complex event-driven scenarios. The broker ensures that events are delivered reliably even in the presence of network failures or service outages.

### Core Components

The system implements three fundamental components that demonstrate the essential elements of Event-Driven Architecture:

**Event Models** define the structure and semantics of events flowing through the system. The implementation includes comprehensive event definitions for order processing, including `OrderPlaced`, `OrderConfirmed`, and `OrderFulfilled` events. Each event carries sufficient context to enable independent processing by consumers without requiring additional data queries.

**OrderProducer** implements the event production logic using Akka actors. The producer generates realistic order events with random customer and product data, demonstrating how business events are created and published to the event infrastructure. The producer handles RabbitMQ connection management, event serialization, and error handling to ensure reliable event publication.

**OrderConsumer** implements event consumption and processing logic. The consumer subscribes to order events from RabbitMQ and simulates realistic order fulfillment processes including inventory checking, payment processing, and shipping coordination. The implementation demonstrates how consumers can process events independently while maintaining system consistency.

### Event Flow Architecture

The system implements a topic-based event routing pattern using RabbitMQ exchanges and queues. Events are published to a topic exchange with specific routing keys that enable flexible event routing based on event types and characteristics. This pattern supports both point-to-point and publish-subscribe communication patterns within the same infrastructure.

Event serialization uses a custom JSON-based format that balances human readability with processing efficiency. The serialization format includes event metadata such as timestamps, event IDs, and correlation IDs that support event tracing and debugging. The format is designed to be forward-compatible, allowing for event schema evolution without breaking existing consumers.

Error handling and resilience are built into every component of the system. The producer implements retry logic with exponential backoff for handling temporary RabbitMQ connection failures. The consumer uses acknowledgment-based processing to ensure that events are not lost if processing fails. Dead letter queues handle events that cannot be processed after multiple retry attempts.

## Getting Started

### Prerequisites

Before running the system, ensure you have the following software installed:

- **Java 8 or higher** - Required for running Scala applications
- **Scala 2.13.6** - The programming language runtime
- **SBT (Scala Build Tool)** - For building and running the project
- **RabbitMQ Server** - The message broker for event routing

### Installing RabbitMQ

**On Ubuntu/Debian:**
```bash
sudo apt-get update
sudo apt-get install rabbitmq-server
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server
```

**On macOS:**
```bash
brew install rabbitmq
brew services start rabbitmq
```

**On Windows:**
Download and install RabbitMQ from the official website: https://www.rabbitmq.com/download.html

### Building the Project

1. Clone or download the project to your local machine
2. Navigate to the project directory:
   ```bash
   cd scala-eda-ecommerce
   ```
3. Compile the project using SBT:
   ```bash
   sbt compile
   ```

### Running the System

The system consists of two main applications that demonstrate the producer-consumer pattern:

**Step 1: Start the Consumer**
Open a terminal and run the consumer application:
```bash
sbt "runMain ConsumerApp"
```

The consumer will start listening for events from RabbitMQ and display processing information as events are received.

**Step 2: Run the Producer**
Open another terminal and run the producer application:
```bash
sbt "runMain ProducerApp"
```

The producer will generate several random orders and publish them as events to RabbitMQ. You should see the events being processed by the consumer in the first terminal.

### Expected Output

**Producer Output:**
```
=== Event-Driven Architecture E-commerce Order Producer ===
Starting order generation...
✅ Order 1: Successfully placed order order-a1b2c3d4 with event event-e5f6g7h8
✅ Order 2: Successfully placed order order-i9j0k1l2 with event event-m3n4o5p6
...
=== Order generation completed ===
```

**Consumer Output:**
```
=== Event-Driven Architecture E-commerce Order Consumer ===
Starting order processing service...
📦 Processing Order: order-a1b2c3d4
👤 Customer: John Doe
💰 Total Amount: $1299.97
  1. Validating order details ✅
  2. Checking inventory availability ✅
  3. Processing payment ✅
  4. Preparing items for shipment ✅
  5. Generating shipping label ✅
  6. Order fulfillment completed ✅
🎉 Order order-a1b2c3d4 successfully fulfilled!
```

## System Architecture

The system architecture demonstrates the key principles of Event-Driven Architecture through a clean separation of concerns and well-defined component interactions.

### Component Diagram

```
┌─────────────────┐    Events    ┌─────────────────┐    Events    ┌─────────────────┐
│   Producer      │─────────────▶│   RabbitMQ      │─────────────▶│   Consumer      │
│   (OrderApp)    │              │   Message       │              │   (Fulfillment) │
│                 │              │   Broker        │              │                 │
└─────────────────┘              └─────────────────┘              └─────────────────┘
        │                                │                                │
        │                                │                                │
        ▼                                ▼                                ▼
┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
│ Akka Actor      │              │ Topic Exchange  │              │ Akka Actor      │
│ System          │              │ + Queue         │              │ System          │
└─────────────────┘              └─────────────────┘              └─────────────────┘
```

### Event Flow

1. **Event Generation**: The OrderProducer actor generates OrderPlaced events containing comprehensive order information
2. **Event Publication**: Events are serialized to JSON and published to RabbitMQ topic exchange
3. **Event Routing**: RabbitMQ routes events to appropriate queues based on routing keys
4. **Event Consumption**: OrderConsumer actors subscribe to queues and process events asynchronously
5. **Event Processing**: Consumers simulate order fulfillment and publish additional events as needed

### Data Models

The system defines comprehensive data models that represent real-world e-commerce entities:

**Customer**: Represents customer information including ID, name, and email
**Product**: Represents product catalog items with pricing and categorization
**Order**: Aggregates customer information, ordered items, and order metadata
**OrderItem**: Represents individual items within an order with quantity and pricing
**Events**: Immutable event objects that capture state changes and business occurrences

### Configuration

The system uses centralized configuration through the `RabbitMQConfig` object, which defines:

- **Connection Parameters**: Host, port, credentials for RabbitMQ connectivity
- **Exchange Configuration**: Topic exchange for flexible event routing
- **Queue Configuration**: Durable queues for reliable event storage
- **Routing Keys**: Semantic routing patterns for different event types

### Error Handling and Resilience

The implementation includes comprehensive error handling and resilience patterns:

**Connection Resilience**: Automatic retry logic for RabbitMQ connection failures with exponential backoff
**Message Acknowledgment**: Consumer acknowledgment ensures events are not lost during processing failures
**Dead Letter Queues**: Failed events are routed to dead letter queues for manual investigation
**Circuit Breaker Pattern**: Prevents cascading failures by isolating failing components
**Graceful Degradation**: System continues operating with reduced functionality during partial failures

### Monitoring and Observability

The system includes extensive logging and monitoring capabilities:

**Structured Logging**: All components use structured logging with correlation IDs for event tracing
**Metrics Collection**: Key performance indicators are collected for system monitoring
**Event Auditing**: Complete audit trail of all events and processing activities
**Health Checks**: Component health monitoring for proactive failure detection

## References

[1] Martin Fowler. "Event-Driven Architecture." https://martinfowler.com/articles/201701-event-driven.html

[2] Chris Richardson. "Microservices Patterns: With Examples in Java." Manning Publications, 2018.

[3] Vaughn Vernon. "Reactive Messaging Patterns with the Actor Model." Addison-Wesley Professional, 2015.

[4] Bobby Calderwood. "Event Sourcing: What it is and why it's awesome." https://dev.to/barryosull/event-sourcing-what-it-is-and-why-its-awesome

[5] RabbitMQ Documentation. "RabbitMQ Tutorials." https://www.rabbitmq.com/getstarted.html

[6] Akka Documentation. "Akka Actor Typed." https://doc.akka.io/docs/akka/current/typed/index.html

[7] Sam Newman. "Building Microservices: Designing Fine-Grained Systems." O'Reilly Media, 2021.

[8] Gregor Hohpe and Bobby Woolf. "Enterprise Integration Patterns." Addison-Wesley Professional, 2003.

---
