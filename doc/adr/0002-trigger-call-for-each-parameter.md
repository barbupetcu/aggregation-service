# 2. Trigger a webclient call for each parameter received on request

Date: 2024-03-05

## Context

In the requirements it's requested to include all parameters with null value which failed due to either error or timeout

## Decision

Trigger a webclient request for each parameter received, so we find each parameter which failed. However, the client is developed to accept a Set of parameters which will be useful for AS-2 where we will need to trigger a single request for multiple parameters  

## Consequences

The response might be slower from all services might be slower
The response will contain all the parameters sent by the client with null value in case when the request to backend service failed