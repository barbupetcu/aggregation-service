# 1. Usage of spring webflux framework

Date: 2024-03-05

## Context

We need to aggregate all the response from backend services into a single network call. The naive approach would be to call each service one after other or in different threads and then join them after. The problem is that request thread get blocked until all responses are ready which can lead to slow service responses.   

## Decision

Used spring webflux framework to take advantage of non-blocking client and server.

## Consequences

Network traffic is optimised without performance costs