#!/usr/bin/env bash

run() {
    local version="$1"

    ./gradlew ":${version}:runClient" --no-daemon 2>&1 |
    while IFS= read -r l; do
        echo "$l"
        if [[ $l == *"Setting user:"* ]]; then
            pkill -f net.fabricmc.devlaunchinjector.Main
            break
        fi
    done
}

run 1.21
run 1.21.11