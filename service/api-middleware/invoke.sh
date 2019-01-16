#!/bin/bash
# . invoke [EventPath]

EventPath=${1:-src/example/event.json}

sam local invoke -e $EventPath