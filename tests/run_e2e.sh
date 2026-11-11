#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."

binary=$(find build -maxdepth 3 -type f -name 'conf-uprav-2.kexe' -path '*LinuxX64*' | head -n 1)
if [ -z "$binary" ]; then
    echo "application binary is missing, run 'make build' first" >&2
    exit 1
fi

work=$(mktemp -d)
registry_port=18765
registry_pid=

cleanup() {
    if [ -n "$registry_pid" ]; then
        kill "$registry_pid" 2>/dev/null || true
    fi
    rm -rf "$work"
}
trap cleanup EXIT

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

expect_output() {
    name=$1
    expected=$2
    shift 2
    "$binary" "$@" >"$work/$name.out" 2>"$work/$name.err" || fail "$name: unexpected exit code"
    diff -u "$expected" "$work/$name.out" || fail "$name: output mismatch"
}

expect_failure() {
    name=$1
    expected_status=$2
    expected_fragment=$3
    shift 3
    set +e
    "$binary" "$@" >"$work/$name.out" 2>"$work/$name.err"
    status=$?
    set -e
    [ "$status" -eq "$expected_status" ] || fail "$name: expected exit code $expected_status, got $status"
    grep -q "$expected_fragment" "$work/$name.err" || fail "$name: missing '$expected_fragment' in stderr"
}

start_registry() {
    python3 -m http.server "$registry_port" --bind 127.0.0.1 \
        --directory tests/fixtures/npm/registry >"$work/registry.log" 2>&1 &
    registry_pid=$!
    attempts=0
    while ! curl -fs "http://127.0.0.1:$registry_port/root-package" >/dev/null 2>&1; do
        attempts=$((attempts + 1))
        [ "$attempts" -lt 50 ] || fail "local registry did not start"
        sleep 0.1
    done
}

write_registry_config() {
    package_name=$1
    config_path=$2
    load_order=${3:-false}
    cat >"$config_path" <<EOF
package: $package_name
repository: "http://127.0.0.1:$registry_port"
repository_mode: registry
image_file: graph.png
ascii_tree: false
filter: ""
load_order: $load_order
EOF
}

expect_output help-page tests/expected/help.txt --help
expect_failure invalid-boolean 1 "must be a boolean" \
    --config tests/fixtures/configs/invalid-boolean.yaml
expect_failure missing-parameter 1 "parameter 'filter' is required" \
    --config tests/fixtures/configs/invalid-missing-filter.yaml
expect_failure unknown-argument 2 "unknown argument '--unknown'" --unknown
expect_failure missing-file 1 "does not exist" --config tests/fixtures/configs/missing.yaml

expect_output stage3-linear tests/expected/stage3-linear.txt \
    --config tests/fixtures/configs/test-linear.yaml
expect_output stage3-diamond tests/expected/stage3-diamond.txt \
    --config tests/fixtures/configs/test-diamond.yaml
expect_output stage3-cycle tests/expected/stage3-cycle.txt \
    --config tests/fixtures/configs/test-cycle.yaml
expect_output stage3-self-cycle tests/expected/stage3-self-cycle.txt \
    --config tests/fixtures/configs/test-self-cycle.yaml
expect_output stage3-filter tests/expected/stage3-filter.txt \
    --config tests/fixtures/configs/test-filter.yaml
expect_output stage4-order-linear tests/expected/stage4-order-linear.txt \
    --config tests/fixtures/configs/test-linear-order.yaml
expect_output stage4-order-diamond tests/expected/stage4-order-diamond.txt \
    --config tests/fixtures/configs/test-diamond-order.yaml
expect_output stage4-order-cycle tests/expected/stage4-order-cycle.txt \
    --config tests/fixtures/configs/test-cycle-order.yaml
expect_output stage4-order-filter tests/expected/stage4-order-filter.txt \
    --config tests/fixtures/configs/test-filter-order.yaml
expect_output stage4-print-config tests/expected/stage4-print-config.txt \
    --print-config --config tests/fixtures/configs/registry.yaml
expect_failure unknown-package 1 "is not declared in the test repository" \
    --config tests/fixtures/configs/test-unknown-package.yaml
expect_failure invalid-repository 1 "test repository line 2" \
    --config tests/fixtures/configs/test-invalid-repository.yaml

start_registry
write_registry_config root-package "$work/registry.yaml"
write_registry_config root-package "$work/registry-order.yaml" true
write_registry_config missing-package "$work/missing-package.yaml"

expect_output stage3-registry tests/expected/stage3-registry.txt \
    --config "$work/registry.yaml"
expect_output stage4-order-registry tests/expected/stage4-order-registry.txt \
    --config "$work/registry-order.yaml"
expect_failure registry-not-found 1 "was not found in registry" \
    --config "$work/missing-package.yaml"

echo "All end-to-end checks passed"
