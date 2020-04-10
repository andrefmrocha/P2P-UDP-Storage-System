#!/bin/bash

# print usage
if [ "$#" -lt 2 -o "$#" -gt 4 ]; then
  echo "Usage: $0 PEER_AP SUB_PROTOCOL <OPND_1> <OPND_2>" >&2
  echo "  PEER_AP is the peer access point"
  echo "  SUB_PROTOCOL is the protocol to use"
  echo "  OPND_1 is an optional first operand"
  echo "  OPND_2 is an optional second operand"
  exit 1
fi

peer_ap=$1
sub_protocol=$2
opnd_1=$3
opnd_2=$4
java -cp out/production/sdis1920-t1g02/ com.feup.sdis.client.Client $peer_ap $sub_protocol $opnd_1 $opnd_2 >> outputs/clients.txt