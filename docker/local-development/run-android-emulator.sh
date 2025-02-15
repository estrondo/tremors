#!/bin/sh

DOMAINS="local.estrondo.one,local-tremors-api.estrondo.one"

case $1 in
  "")
      echo "AVDS:"
      emulator -list-avds
      ;;
  *)
      echo "I need your root power!"
      sudo echo "Starting DNS Server..."
      sudo dnsmasq -d -h --host-record=$DOMAINS,10.0.2.2 &
      sleep 3

      # reference: https://stackoverflow.com/questions/16965089/getting-pid-of-process-in-shell-script
      DNS_MASQ_PID=`/bin/ps -fu root | grep -v "sudo" | grep "dnsmasq" | grep -v "grep" | awk '{print $2}'`

      echo "dnsmasq PID=$DNS_MASQ_PID"
      sleep 3
      emulator "@$1" -dns-server 127.0.0.1

      echo "I'm about to kill dnsmasq"
      sleep 3
      sudo kill -9 "$DNS_MASQ_PID"
      ;;
esac