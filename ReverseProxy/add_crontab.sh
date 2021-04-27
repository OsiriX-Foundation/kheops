#!/bin/bash

$(while :; do /logcrontab.sh; sleep "60m"; done;) &
