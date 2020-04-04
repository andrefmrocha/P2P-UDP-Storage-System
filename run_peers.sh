#!/bin/bash

mkdir outputs &> /dev/null

(java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer 1 > outputs/p1.txt)&
PEER_1_PID=$!

(java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer 2 > outputs/p2.txt)&
PEER_2_PID=$!

echo "Press 'q' to end the peers processes";
while true; do
    read -t 0.25 -N 1 input
    if [[ $input = "q" ]]; then
        echo
        break
    fi
done

# kill processes
kill $PEER_1_PID
kill $PEER_2_PID