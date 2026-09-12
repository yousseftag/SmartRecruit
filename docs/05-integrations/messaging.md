# Messaging

## Purpose

RabbitMQ decouples the backend from the FastAPI engine. Tasks are published to request queues and results returned on a response queue, all on a single direct exchange.

## Topology

| Queue | Exchange | Routing key | Direction | Producer | Consumer |
|-------|----------|-------------|-----------|----------|----------|
| `cv.processing.queue` | `ai.exchange` | `cv.routing.key` | Request | `CvIngestionProducer` | FastAPI engine / `SimulationNlpService` |
| `offer.processing.queue` | `ai.exchange` | `offer.routing.key` | Request | `OfferIngestionProducer` | FastAPI engine / `SimulationOfferService` |
| `cv.sync.queue` | `ai.exchange` | `cv.sync.routing.key` | Result | FastAPI engine / `SimulationNlpService` | `CvSyncConsumer` (`@RabbitListener`) |

All queues are durable. Messages use the Jackson JSON converter.

```mermaid
flowchart LR
    subgraph App["Spring Boot"]
        P1["CvIngestionProducer"]
        P2["OfferIngestionProducer"]
        C1["CvSyncConsumer"]
    end
    subgraph Broker["RabbitMQ — ai.exchange"]
        Q1[("cv.processing.queue")]
        Q2[("offer.processing.queue")]
        Q3[("cv.sync.queue")]
    end
    AI["FastAPI engine / simulator"]
    P1 -- cv.routing.key --> Q1 --> AI
    P2 -- offer.routing.key --> Q2 --> AI
    AI -- cv.sync.routing.key --> Q3 --> C1

    classDef be fill:#6DB33F,fill-opacity:0.18,stroke:#8ED16A,stroke-width:2px;
    classDef mq fill:#FF6600,fill-opacity:0.18,stroke:#FF8A3D,stroke-width:2px;
    classDef ai fill:#009688,fill-opacity:0.18,stroke:#26C6B6,stroke-width:2px;
    class P1,P2,C1 be;
    class Q1,Q2,Q3 mq;
    class AI ai;
```

## Listener retry

Configured under `spring.rabbitmq.listener.simple`:

- `default-requeue-rejected: false` — a failed message is not requeued indefinitely.
- Retry: enabled, 3 attempts, 2 s initial interval, ×2 multiplier, 10 s cap.

## Simulation mode

When `AI_SIMULATION_ENABLED=true`, both simulators are registered as AMQP listeners inside the backend JVM. Both follow a deterministic **2-success / 1-fail** cycle:

- `SimulationNlpService` consumes `cv.processing.queue`, waits 6–9 s, and publishes a result to `cv.sync.queue`.
- `SimulationOfferService` consumes `offer.processing.queue`, waits 2–3 s, and posts the result to the offer sync endpoint.

This exercises the real broker without the external FastAPI engine. Set the flag to `false` to disable both.

## Why a queue instead of direct HTTP

Durability, decoupling, and backpressure — rationale in [Design Decisions](../08-appendices/decisions.md#messaging--ai-flow).

## Related

- [AI Contract](ai-contract.md)
- [Configuration](../07-operations/configuration.md)
- [CV Ingestion](../04-features/cv-ingestion.md)
