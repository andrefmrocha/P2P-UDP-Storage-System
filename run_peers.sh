#!/bin/bash

# print usage
if [ "$#" != 1 -a "$#" != 2 -a "$#" != 8 ]; then
  echo "Usage: $0 N_PEERS <VERSION> <MC_ADDR MC_PORT MDB_ADDR MDB_PORT MDR_ADDR MDR_PORT>"
  echo "  N_PEERS is the number of peers to start"
  echo "    - IDs and Access Points will be assigned incrementally, starting at 1"
  echo "    - For example, N_PEERS = 4 starts peers with IDs and Access Points 1,2,3,4"
  echo "  VERSION is the peer protocol version, 1.0 is base, 1.1 is enhanced. By default its 1.0."
  echo ""
  echo "  The remaining arguments are optional, but must be given together: "
  echo "  MC_ADDR is the multicast address to be used for the MC channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDB_ADDR is the multicast address to be used for the MDB channel"
  echo "  MC_PORT is the port to be used for the MC channel"
  echo "  MDR_ADDR is the multicast address to be used for the MDR channel"
  echo "  MDR_PORT is the port to be used for the MDR channel"
  exit 1
fi

# default values
N_PEERS=$1
VERSION='1.0'
MC_ADDR='224.0.0.0'
MC_PORT=8080
MDB_ADDR='224.0.0.1'
MDB_PORT=8080
MDR_ADDR='224.0.0.2'
MDR_PORT=8080

if [ "$#" == 2 ]; then
  VERSION=$2
elif [ "$#" == 8 ]; then
  VERSION=$2
  MC_ADDR=$3
  MC_PORT=$4
  MDB_ADDR=$5
  MDB_PORT=$6
  MDR_ADDR=$7
  MDR_PORT=$8
fi

# create output folder
mkdir outputs &> /dev/null

# start processes
echo "Starting $N_PEERS peers"
for (( id=1; id<=$N_PEERS; id++ ))
do
    (java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $VERSION $id $id $MC_ADDR $MC_PORT $MDB_ADDR $MDB_PORT $MDR_ADDR $MDR_PORT &>> outputs/p$id.txt)&
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
