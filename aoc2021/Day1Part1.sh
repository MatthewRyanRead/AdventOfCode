#!/bin/bash

# this was off-by-one and I didn't bother to debug, moved to Java instead
echo $(( 1 + $(cat ./inputs/day1.txt | \
    while read line; do
        if [[ "$PREV_DEPTH" != "" ]]; then
            if (( $PREV_DEPTH < $line )); then
                echo 1
            fi
        fi
        PREV_DEPTH=$line
    done | \
    wc -l) ))