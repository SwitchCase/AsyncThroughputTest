# AsyncThroughputTest
This repo tests CompleteableFuture against regular Futures. We do this test under a hypothetical scenario where the user calls an orchestration API to make milk tea. The orchestration API depends on another service to perform individual actions (boil water, brew tea, boil milk, combine milk and tea). The orchestration is as perL
a. Chain 1: Boil water -> Brew Tea.
b. Chain 2: Boil Milk.
c. Combine chain 1 and chain 2 do a combine operation.

We do this through 3 components:
* Python Service that responds to HTTP calls (individual actions)
* Java Service that calls the python service via 2 endpoints (async and sync). This is the orchestrator and the service under test.
* K6 based load test that calls the Java service.

## Python Service
* Supports 4 endpoints. Each endpoint returns a mock response after a 100ms sleep.

## Java Service
* Supports 2 endpoints:
* * POST /sync: The orchestration is constructed using blocking calls to the Py service using Futures.
* * POST /async: The orchestration is constructed using non-blocking callbacks through CompleteableFutures.

## K6 Load Test
* Creates random requests to hit the desired endpoint.

## How To Run
Run the following steps in order:
* To start the Python/Docker based server: ```./scripts/start-py-server```
* To start the SpringBoot based app server: ```./scripts/start-spring-server```
* To run the load tests:
    * Sync load tests: ```./scripts/start-load-tests-sync --duration 1m --vus 100``` for 1 minute with 100 vus.
    * Async load tests: ```./scripts/start-load-tests-async --duration 1m --vus 100``` for 1 minute with 100 vus.
