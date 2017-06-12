# Prerequisites
- Docker
- docker-compose

# How to Run This
The basic demo runs four services that log ctrace-style logs to stdout. Start this by `$ docker-compose up`. You will remain attached to all four containers' logs. Execute `run.sh` to make calls against the running services. You'll see those services' logs in the same terminal where you launched docker-compose.

The advanced demo runs those same four services, but they log to a file. Start the demo with `docker-compose -f docker-compose.yml -f docker-compose-zipkin.yml up`. In this case, Flink is tailing the log files; it performs some minimal transformation, then it submits those to Zipkin. You can see the Zipkin UI at `http://localhost:9411`. Flink is running at `http://localhost:8081`. Docker waits a bit before submitting the Flink job to the cluster, so there will be a delay before Flink begins processing. As before, execute `run.sh` to make calls against the running services. Stop this via `$ docker-compose -f docker-compose.yml -f docker-compose-zipkin.yml down`.

# How to Stop This
`docker-compose down`

# ctrace-demos
Canonical OpenTracing Demos

- [ ] Node.js Gateway (forwards on to dynamically selected language Service)
- [ ] Node.js Service
- [ ] Node.js Kinesis Lambda (receives from Service if enabled)
- [ ] Node.js API Gateway Lambda (alternative for Service)
- [ ] Go Gateway (forwards on to dynamically selected language Service)
- [ ] Go Service
- [ ] Java Gateway (forwards on to dynamically selected language Service)
- [ ] Java Service
- [ ] Java Kinesis Lambda (receives from Service if enabled)
- [ ] Java API Gateway Lambda (alternative for Service)
- [ ] Python Gateway (forwards on to dynamically selected language Service)
- [ ] Python Service
- [ ] Python Kinesis Lambda (receives from Service if enabled)
- [ ] Python API Gateway Lambda (alternative for Service)
