#!/bin/sh

#crontab /etc/crontabfile

$(while :; do /logcrontab; sleep "5m"; done;) &