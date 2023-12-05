#!/bin/bash

kotlinc Day$1.kt -include-runtime -d Day$1.jar
time java -jar Day$1.jar
