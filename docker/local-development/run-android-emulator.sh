#!/bin/sh

DOMAINS="local.estrondo.one,local.tremors-api.estrondo.one"

case $1 in
  "")
      echo "AVDS:"
      emulator -list-avds
      ;;
  *)
      sudo dnsmasq -d -h --host-record=$DOMAINS,10.0.2.2 &
      sleep 3

      # reference: https://stackoverflow.com/questions/16965089/getting-pid-of-process-in-shell-script
      DNS_SERVER_PID=`/bin/ps -fu root | grep -v "sudo" | grep "dnsmasq" | grep -v "grep" | awk '{print $2}'`

      echo "DNS Server has been started with PID=$DNS_SERVER_PID"
      emulator "@$1" -dns-server 127.0.0.1

      echo "killing DNS Server..."
      sudo kill -9 "$DNS_SERVER_PID"
      ;;
esac