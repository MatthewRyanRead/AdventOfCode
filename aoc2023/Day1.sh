#!/bin/bash

solve() {
    echo "$1" | sed 's/[^0-9]//g' \
        | while read line; do echo "${line:0:1}${line: -1}"; done \
        | gawk '{s+=$1;} END {print s;}'
}

input=$(cat inputs/Day1.txt)
part2=$(echo "$input" | sed 's/one/one1one/g' | sed 's/two/two2two/g' | sed 's/three/three3three/g' | sed 's/four/four4four/g' | sed 's/five/five5five/g' | sed 's/six/six6six/g' | sed 's/seven/seven7seven/g' | sed 's/eight/eight8eight/g' | sed 's/nine/nine9nine/g')

echo Part 1: $(solve "$input")
echo Part 2: $(solve "$part2")
