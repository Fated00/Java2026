.PHONY: run test docker-build docker-run docker-up docker-clean

run:
	./gradlew run

test:
	./gradlew clean test check

docker-build:
	docker compose build

docker-run:
	docker compose run --rm autosalon-app

docker-up:
	docker compose up --build --abort-on-container-exit --exit-code-from autosalon-app

docker-clean:
	docker compose down --remove-orphans
