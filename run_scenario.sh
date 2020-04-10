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

stop_peers() {
  echo 'q' > $FIFO_PATH
  sleep 0.1
  rm $FIFO_PATH
}

get_state()
{
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
  run_peers 5 $ENHANCED
  sleep 1

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  run_client 1 RESTORE testfile.txt
  run_client 2 RESTORE testfile.txt
  run_client 2 RESTORE t3-citasdyplan.zip
  sleep 2
  get_state 1 2 3 4 5

  run_client 1 DELETE README.md
  run_client 1 DELETE READasdME.md
  run_client 1 DELETE testfile.txt
  sleep 2
  get_state 1 2 3 4 5

  sleep 2
  stop_peers

# reclaim tests
elif [ $SCENARIO == 2 ]; then
  run_peers 5 $ENHANCED
  sleep 1

  run_client 1 BACKUP testfile.txt 3
  sleep 2
  get_state 1 2 3 4 5

  run_client 2 RECLAIM 64
  sleep 2
  get_state 1 2 3 4 5

  run_client 2 RECLAIM 0
  sleep 2
  get_state 1 2 3 4 5

  run_client 2 RECLAIM 64000
  sleep 2
  get_state 1 2 3 4 5

  run_client 3 RECLAIM 0
  sleep 4
  get_state 1 2 3 4 5

  run_client 1 RECLAIM 0
  run_client 2 RECLAIM 0
  sleep 4
  get_state 1 2 3 4 5

  sleep 2
  stop_peers

# backup same file on different peers
elif [ $SCENARIO == 3 ]; then
  run_peers 5 $ENHANCED
  sleep 1

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  run_client 5 DELETE testfile.txt
  sleep 1

  run_client 5 BACKUP testfile.txt 3
  sleep 2

  get_state 1 2 3 4 5

  run_client 5 DELETE testfile.txt
  sleep 1

  get_state 1 2 3 4 5

  sleep 2
  stop_peers

# test repl counter info is kept between executions
elif [ $SCENARIO == 4 ]; then
  run_peers 5 $ENHANCED
  sleep 0.5

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  stop_peers
  sleep 0.5

  run_peers 5 $ENHANCED
  sleep 0.5

  run_client 1 BACKUP testfile.txt 3
  sleep 2

  stop_peers

else
  echo 'Invalid scenario specified'
fi