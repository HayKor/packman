#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."

binary=$(find build -maxdepth 3 -type f -name 'conf-uprav-2.kexe' -path '*LinuxX64*' | head -n 1)
if [ -z "$binary" ]; then
    echo "application binary is missing, run 'make build' first" >&2
    exit 1
fi

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

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

expect_output valid-config tests/expected/stage1-parameters.txt \
    --config tests/fixtures/configs/registry.yaml
expect_failure invalid-boolean 1 "must be a boolean" \
    --config tests/fixtures/configs/invalid-boolean.yaml
expect_failure missing-parameter 1 "parameter 'filter' is required" \
    --config tests/fixtures/configs/invalid-missing-filter.yaml
expect_failure unknown-argument 2 "unknown argument '--unknown'" --unknown
expect_failure missing-file 1 "does not exist" --config tests/fixtures/configs/missing.yaml
expect_output help-page tests/expected/help.txt --help

echo "All end-to-end checks passed"
