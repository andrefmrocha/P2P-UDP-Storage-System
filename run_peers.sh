#!/bin/bash

# print usage
if [ "$#" -lt 1 -o "$#" -gt 2 ]; then
  echo "Usage: $0 N_PEERS <ENHANCED>" >&2
  echo "  N_PEERS is the number of peers to start"
  echo "  IDs will be assigned incrementally starting at 1"
  echo "  For example, N_PEERS = 4 starts peers 1,2,3,4"
  echo "  ENHANCED is an optional flag signaling if the ENHANCED protocol should be used, 1 for true"
  exit 1
fi

# enhanced flag
ENHANCED=''
if [ "$#" == 2 -a "$2" == 1 ]; then
  echo 'Using enhanced protocol'
  ENHANCED="ENHANCED"
fi

# create output folder
mkdir outputs &> /dev/null

# start processes
N_PEERS=$1
echo "Starting $N_PEERS peers"
for (( id=1; id<=$N_PEERS; id++ ))
do
    (java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $id $ENHANCED &>> outputs/p$id.txt)&
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
