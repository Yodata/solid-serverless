#!/usr/bin/env bash

EventPath=${1:-src/example/event.json}

sam local invoke -e $EventPath