#!/bin/bash

# print usage
if [ "$#" != 8 ]; then
  echo "Usage: $0 N_PEERS VERSION MC_ADDR MC_PORT MDB_ADDR MDB_PORT MDR_ADDR MDR_PORT"
  echo "  N_PEERS is the number of peers to start"
  echo "    - IDs and Access Points will be assigned incrementally, starting at 1"
  echo "    - For example, N_PEERS = 4 starts peers with IDs and Access Points 1,2,3,4"
  echo "  VERSION is the peer protocol version, 1.0 is base, 1.1 is enhanced. By default its 1.0."
  echo "  MC_ADDR is the multicast address to be used for the MC channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDB_ADDR is the multicast address to be used for the MDB channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDR_ADDR is the multicast address to be used for the MDR channel"
  echo "  MDR_PORT is the port to be used for the MDR channel"
  exit 1
fi

# create output folder
mkdir outputs &> /dev/null

# start processes
N_PEERS=$1
echo "Starting $N_PEERS peers"
for (( id=1; id<=$N_PEERS; id++ ))
do
    (java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $2 $id $id $3 $4 $5 $6 $7 $8 &>> outputs/p$id.txt)&
    pids[$id]=$!
done

# wait for input to kill processes
echo "Press 'q' to end the peers processes";
while true; do
    read -t 0.25 -N 1 input
    if [[ $input = "q" ]]; then
        echo
        break
    fi
done

# kill processes
echo "Ending peers processes"
for (( id=1; id<=$N_PEERS; id++ ))
do
    kill ${pids[$id]}
done
