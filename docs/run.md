## Docker
### Single Server
```bash
docker compose -f docker-compose.yml up -d
```
### With Companion Simple Attribute Store Server
```bash
docker compose -f docker-compose.yml -f docker-compose-both.yml up -d
```
### With Companion Wiremock Server
```bash
docker compose -f docker-compose.yml -f docker-compose-with-wiremock.yml up -d
```

## Intellij
If using Intellij, you can make use of the following run configurations ([here](../.idea/runConfigurations))

### Run with local configuration
Running io.telicent.attribute.evaluator.ALEServer with the following argument:
```
--config "file:src/test/resources/test_config.json"
```
### Run with local store

Running io.telicent.attribute.evaluator.ALEServer with the following argument:
```
--store "file:src/main/resources/sample_attributes.ttl"
```