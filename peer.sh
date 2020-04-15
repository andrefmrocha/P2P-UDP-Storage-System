#!/bin/bash

# print usage
if [ "$#" != 9 ]; then
  echo "Usage: $0 <version> <peer_id> <svc_access_point> <mc_addr> <mc_port> <mdb_addr> <mdb_port> <mdr_addr> <mdr_port>"
  echo "  version is the peer protocol version, 1.0 is base, 1.1 is enhanced. By default its 1.0."
  echo "  peer_id is the ID of the peer to start"
  echo "  svc_access_point is the name of the RMI access point to create"
  echo "  mc_addr is the multicast address to be used for the MC channel"
  echo "  mc_port is the port to be used for the MC channel"
  echo "  mdb_addr is the multicast address to be used for the MDB channel"
  echo "  mdb_port is the port to be used for the MDB channel"
  echo "  mdr_addr is the multicast address to be used for the MDR channel"
  echo "  mdr_port is the port to be used for the MDR channel"
  exit 1
fi

java -cp build/ com.feup.sdis.peer.Peer $1 $2 $3 $4 $5 $6 $7 $8 $9