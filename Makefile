.PHONY: run test db-up db-down docker-build docker-run docker-up docker-clean

run:
	./gradlew bootRun

test:
	./gradlew clean test check

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

docker-build:
	docker compose build

docker-run:
	docker compose run --rm autosalon-app

docker-up:
	docker compose up --build --abort-on-container-exit --exit-code-from autosalon-app

docker-clean:
	docker compose down --remove-orphans
