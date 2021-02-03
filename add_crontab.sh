#!/bin/bash

$(while :; do /logcrontab.sh; sleep "1m"; done;) &
