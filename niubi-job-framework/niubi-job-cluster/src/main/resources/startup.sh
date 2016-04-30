#!/bin/sh

bootstrap_sh=bootstrap.sh
chmod +x ${bootstrap_sh}
nohup ./bootstrap.sh start >/dev/null 2>&1 &
echo niubi-job has been started...