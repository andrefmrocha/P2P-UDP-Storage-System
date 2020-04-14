#!/bin/bash

# print usage
if [ "$#" -lt 1 -o "$#" -gt 2 ]; then
  echo "Usage: $0 PEER_ID <ENHANCED>" >&2
  echo "  PEER_ID is the ID of the peer to start"
  echo "  ENHANCED is an optional flag signaling if the ENHANCED protocol should be used, 1 for true"
  exit 1
fi

# enhanced flag
ENHANCED=''
if [ "$#" == 2 -a "$2" == 1 ]; then
  echo 'Using enhanced protocol'
  ENHANCED="ENHANCED"
fi

echo "Starting peer with ID $1"
java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $1 $ENHANCED