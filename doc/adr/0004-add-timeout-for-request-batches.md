# 4. Add timeout for each batch to avoid long aggregation responses

Date: 2024-03-05

## Context

In order to avoid long responses from aggregation services due to capping not being reached we need to schedule the queues to push events at each 5 seconds

## Decision

Use buffer timeout Flux's functionality which means that the event is emitted either when the buffer cap is reached either when the timeout expired. 

## Consequences

In some cases the batch send to the backend service could be lower than the configured threshold which might increase network traffic on backend services.
We avoid long response times for aggregation services when the traffic is lower and the chances to reach cap is limited.