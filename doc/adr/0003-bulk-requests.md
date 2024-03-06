# 3. Batch parameters sent to backend services

Date: 2024-03-05

## Context

To prevent overloading of backend services the calls are bulked in fix batches and the response is returned to requester once the batch is completed(from its request or from other requests)

## Decision

Every parameter for each specific api is forwarded to sink queue which contains the parameters and a callback sink for each parameter. Once the sink queue has reached the cap then it will emit a new event with the capped list of parameters which is forwarded to backend service. When backend service responded the callback from the batch is identified using the key received from backend service (backend client should add all the received parameters in the response even if the call failed) and it's emitted a new event on the callback with the value from backend service. 

## Consequences

Backend services are called in batches decreasing number of requests.
In case when the request to backend service with a given batch failed then all the values for the given batch will have null values, even if only one of the item might be responsible for failure.