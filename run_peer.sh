#!/bin/bash

# print usage
if [ "$#" != 9 ]; then
  echo "Usage: $0 VERSION PEER_ID ACCESS_P MC_ADDR MC_PORT MDB_ADDR MDB_PORT MDR_ADDR MDR_PORT"
  echo "  VERSION is the peer protocol version, 1.0 is base, 1.1 is enhanced. By default its 1.0."
  echo "  PEER_ID is the ID of the peer to start"
  echo "  ACCESS_P is the name of the RMI access point to create"
  echo "  MC_ADDR is the multicast address to be used for the MC channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDB_ADDR is the multicast address to be used for the MDB channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDR_ADDR is the multicast address to be used for the MDR channel"
  echo "  MDR_PORT is the port to be used for the MDR channel"
  exit 1
fi

echo "Starting peer with ID $2"
java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $1 $2 $3 $4 $5 $6 $7 $8 $9