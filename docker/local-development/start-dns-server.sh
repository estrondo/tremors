#!/bin/sh

echo "This script starts a DNS server for Android development, remember you need privilege to run sudo."
DOMAINS="local.estrondo.one,local.tremors-api.estrondo.one"

sudo dnsmasq -d -h --host-record="$DOMAINS,10.0.2.2" --log-debug