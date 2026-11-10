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
    while ! curl -fs "http://127.0.0.1:$registry_port/sample-package" >/dev/null 2>&1; do
        attempts=$((attempts + 1))
        [ "$attempts" -lt 50 ] || fail "local registry did not start"
        sleep 0.1
    done
}

write_registry_config() {
    package_name=$1
    config_path=$2
    cat >"$config_path" <<EOF
package: $package_name
repository: "http://127.0.0.1:$registry_port"
repository_mode: registry
image_file: graph.png
ascii_tree: false
filter: ""
EOF
}

expect_output help-page tests/expected/help.txt --help
expect_failure invalid-boolean 1 "must be a boolean" \
    --config tests/fixtures/configs/invalid-boolean.yaml
expect_failure missing-parameter 1 "parameter 'filter' is required" \
    --config tests/fixtures/configs/invalid-missing-filter.yaml
expect_failure unknown-argument 2 "unknown argument '--unknown'" --unknown
expect_failure missing-file 1 "does not exist" --config tests/fixtures/configs/missing.yaml
expect_failure unsupported-test-mode 1 "is not supported yet" \
    --config tests/fixtures/configs/test-linear.yaml

start_registry
write_registry_config sample-package "$work/registry.yaml"
write_registry_config missing-package "$work/missing-package.yaml"

expect_output stage2-direct-dependencies tests/expected/stage2-direct-dependencies.txt \
    --config "$work/registry.yaml"
expect_failure stage2-not-found 1 "was not found in registry" \
    --config "$work/missing-package.yaml"

echo "All end-to-end checks passed"
