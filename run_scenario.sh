#!/bin/bash

# print usage
if [ "$#" != 1 -a "$#" != 2 -a "$#" != 8 ]; then
  echo "Usage: $0 SCENARIO <VERSION> <MC_ADDR MC_PORT MDB_ADDR MDB_PORT MDR_ADDR MDR_PORT>" >&2
  echo "  SCENARIO specifies the execution scenario to run"
  echo "    - Valid values start at 1"
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
SCENARIO=$1
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

run_client() {
  ./test.sh $1 $2 $3 $4 >> outputs/clients.txt
}

FIFO_PATH='/tmp/sdis_proj1.fifo'
run_peers() {
  mkfifo $FIFO_PATH
  (cat $FIFO_PATH | ./run_peers.sh $1 $VERSION $MC_ADDR $MC_PORT $MDB_ADDR $MDB_PORT $MDR_ADDR $MDR_PORT &> /dev/null)&
}

run_peer() {
  id=$1
  (java -cp build/ com.feup.sdis.peer.Peer $VERSION $id $id $MC_ADDR $MC_PORT $MDB_ADDR $MDB_PORT $MDR_ADDR $MDR_PORT &>> outputs/p$id.txt)&
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

echo "Cleaning peers folders..."
clean_peers_folders
mkdir outputs &> /dev/null

echo "Compiling..."
./compile.sh &> /dev/null

# backup, restore and then delete
if [ $SCENARIO == 1 ]; then
  echo "Testing backup, restore and delete protocols"
  echo "Starting 5 peers"
  run_peers 5
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
  echo "Testing reclaim protocol"
  echo "Starting 5 peers"
  run_peers 5
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
  echo "Testing backup of same file on different peers"
  echo "Starting 5 peers"
  run_peers 5
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
  echo "Testing Store info is kept between executions"
  echo "Starting 5 peers"
  run_peers 5
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
  run_peers 5
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
  echo "Testing enhanced DELETE works even if peer is not up immediately"
  echo "Starting peers 100, 101 and 102"
  cd build
  rmiregistry&
  rmi_pid=$!
  cd ..
  sleep 1

  run_peer 100
  pid_100=$!

  run_peer 101
  pid_101=$!

  run_peer 102
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
  run_peer 102
  pid_102=$!
  echo "Waiting for the DELETE message to be resent"
  echo "> sleep 40"
  sleep 40

  echo "Killing all peers"
  kill $rmi_pid
  kill $pid_100
  kill $pid_101
  kill $pid_102

else
  echo 'Invalid scenario specified'
fi