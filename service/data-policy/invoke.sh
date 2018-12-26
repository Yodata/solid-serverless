#!/bin/bash
# . invoke [FunctionName] [EventPath]

FunctionName=${1:-ApplyPolicyFunction}
EventPath=${2:-src/example/event.json}

sam local invoke $FunctionName -e $EventPath
