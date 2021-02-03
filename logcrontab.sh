#!/bin/sh

if [ -d /run/systemd/system ]; then
    exit 0
fi

if [ ! -x /usr/sbin/logrotate ]; then
    exit 0
fi 

/usr/sbin/logrotate /etc/logrotate.d/nginxday
/usr/sbin/logrotate /etc/logrotate.d/nginxsize
EXITVALUE=$?
if [ $EXITVALUE != 0 ]; then
    /usr/bin/logger -t logrotate "ALERT exited abnormally with [$EXITVALUE]"
fi
exit $EXITVALUE
