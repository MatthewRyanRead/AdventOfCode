#!/bin/bash

max1=0
max2=0
max3=0
curr=0

while read -r line; do
    if [[ "$line" == "" ]]; then
        if (( curr > max1 )); then
            max3=$max2
            max2=$max1
            max1=$curr
        elif (( curr > max2 )); then
            max3=$max2
            max2=$curr
        elif (( curr > max3 )); then
            max3=$curr
        fi

        curr=0
    else
        curr=$((curr + line))
    fi
done <<< "$(cat ../resources/inputs/Day1.txt)"

echo "Max: $max1"
echo "Top 3 sum: $((max1 + max2 + max3))"
