#!/bin/bash

# print usage
if [ "$#" -lt 1 -o "$#" -gt 2 ]; then
  echo "Usage: $0 SCENARIO <ENHANCED>" >&2
  echo "  SCENARIO specifies the execution scenario to run"
  echo "  Valid values start at 1"
  echo "  ENHANCED is an optional flag that specifies whether peers should be enhanced, 1 for true"
  exit 1
fi

SCENARIO=$1
ENHANCED=$2

run_client() {
  ./run_client.sh $1 $2 $3 $4 &> /dev/null
}

FIFO_PATH='/tmp/sdis_proj1.fifo'
run_peers() {
  mkfifo $FIFO_PATH
  (cat $FIFO_PATH | ./run_peers.sh $1 $2 &> /dev/null)&
}

run_peer() {
  id=$1
  ENHANCED=$2
  (java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer $id $ENHANCED &>> outputs/p$id.txt)&
}

stop_peers() {
  echo 'q' > $FIFO_PATH
  sleep 0.1
  rm $FIFO_PATH
}

get_state()
{
  echo "Checking state of $# peers"
  for (( arg=1; arg<="$#"; arg++ ))
  do
    run_client ${!arg} STATE
  done
}

clean_peers_folders()
{
  rm -rf ./outputs
  rm -rf ./peers
}

clean_peers_folders
mkdir outputs &> /dev/null

# backup, restore and then delete
if [ $SCENARIO == 1 ]; then
  echo "Starting 5 peers"
  run_peers 5 $ENHANCED
  sleep 1

  echo "Backing up testfile.txt on peer 1"
  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "Restoring testfile.txt through peer 1"
  run_client 1 RESTORE testfile.txt
  echo "Restoring testfile.txt through peer 2, should fail"
  run_client 2 RESTORE testfile.txt
  echo "Restoring non-existent file through peer 2, should fail"
  run_client 2 RESTORE t3-citasdyplan.zip
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Deleting README.md through peer 1, not backed up"
  run_client 1 DELETE README.md
  echo "Deleting non-existent file through peer 1, should fail"
  run_client 1 DELETE READasdME.md
  echo "Deleting README.md through peer 1, should work"
  run_client 1 DELETE testfile.txt
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  sleep 2
  echo "Killing peers"
  stop_peers

# reclaim tests
elif [ $SCENARIO == 2 ]; then
  echo "Starting 5 peers"
  run_peers 5 $ENHANCED
  sleep 1

  echo "Backing up testfile.txt on peer 1"
  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Limiting space on peer 2 to 64KB, only has space for 1 chunk"
  echo "> run_client 2 RECLAIM 64"
  run_client 2 RECLAIM 64
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Limiting space on peer 2 to 0, should delete all chunks"
  echo "> run_client 2 RECLAIM 0"
  run_client 2 RECLAIM 0
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Redifining space on peer 2 to 64000 KB, enough space for all chunks"
  echo "> run_client 2 RECLAIM 64000"
  run_client 2 RECLAIM 64000
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Limiting space on peer 3 to 0, should delete all chunks"
  echo "> run_client 3 RECLAIM 0"
  run_client 3 RECLAIM 0
  echo "> sleep 4"
  sleep 4
  get_state 1 2 3 4 5

  echo "Limiting space on peers 1 and 2 to 0, should delete all chunks"
  echo "> run_client 1 RECLAIM 0"
  run_client 1 RECLAIM 0
  sleep 1
  echo "> run_client 2 RECLAIM 0"
  run_client 2 RECLAIM 0
  echo "> sleep 4"
  sleep 4
  get_state 1 2 3 4 5

  sleep 2
  echo "Killing peers"
  stop_peers

# backup same file on different peers
elif [ $SCENARIO == 3 ]; then
  echo "Starting 5 peers"
  run_peers 5 $ENHANCED
  sleep 1

  echo "Backing up testfile.txt on peer 1"
  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "Backing up testfile.txt on peer 1 again, should not work"
  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "Deleting testfile.txt through peer 5, should not work"
  echo "> run_client 5 DELETE testfile.txt"
  run_client 5 DELETE testfile.txt
  echo "> sleep 1"
  sleep 1

  echo "Backing up testfile.txt on peer 5"
  echo "Backing up testfile.txt on peer 5"
  run_client 5 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2
  get_state 1 2 3 4 5

  echo "Deleting testfile.txt"
  echo "> run_client 5 DELETE testfile.txt"
  run_client 5 DELETE testfile.txt
  echo "> sleep 1"
  sleep 1
  get_state 1 2 3 4 5

  sleep 2
  echo "Killing peers"
  stop_peers

# test Store info is kept between executions
elif [ $SCENARIO == 4 ]; then
  echo "Starting 5 peers"
  run_peers 5 $ENHANCED
  sleep 0.5

  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "Killing peers"
  stop_peers
  sleep 0.5

  echo "Restarting the 5 peers"
  run_peers 5 $ENHANCED
  sleep 0.5

  echo "> run_client 1 BACKUP testfile.txt 3"
  run_client 1 BACKUP testfile.txt 3
  echo "> sleep 2"
  sleep 2

  echo "> run_client 1 RESTORE testfile.txt"
  run_client 1 RESTORE testfile.txt
  echo "> sleep 2"
  sleep 2

  echo "> run_client 1 DELETE testfile.txt"
  run_client 1 DELETE testfile.txt
  echo "> sleep 2"
  sleep 2

  echo "Killing peers"
  stop_peers

# test delete works if peer initially down
elif [ $SCENARIO == 5 ]; then
  echo "Starting peers 100, 101 and 102"
  run_peer 100 'ENHANCED'
  pid_100=$!

  run_peer 101 'ENHANCED'
  pid_101=$!

  run_peer 102 'ENHANCED'
  pid_102=$!
  sleep 0.5

  echo "> run_client 100 BACKUP testfile.txt 2"
  run_client 100 BACKUP testfile.txt 2
  echo "> sleep 2"
  sleep 2

  echo "Killing Peer 102"
  kill $pid_102

  echo "Requesting delete while peer 102 is down"
  echo "> run_client 100 DELETE testfile.txt"
  run_client 100 DELETE testfile.txt
  echo "> sleep 2"
  sleep 2

  echo "Starting peer 102 again"
  run_peer 102 'ENHANCED'
  pid_102=$!
  echo "Waiting for the DELETE message to be resent"
  echo "> sleep 40"
  sleep 40

  echo "Killing all peers"
  kill $pid_100
  kill $pid_101
  kill $pid_102

else
  echo 'Invalid scenario specified'
fi