.PHONY: build run test e2e demo clean

build:
	./kotlin build

run:
	./run.sh --config config.yaml

test:
	./kotlin test

e2e: build
	./tests/run_e2e.sh

demo: build
	./run.sh --config config.yaml

clean:
	./kotlin clean
	rm -rf out
