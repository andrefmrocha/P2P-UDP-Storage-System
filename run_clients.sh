#!/bin/bash

# print usage
if [ "$#" -ne 0 ]; then
  echo "Usage: $0" >&2
  exit 1
fi

mkdir outputs &> /dev/null

# java TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>
run_client()
{
  peer_ap=$1
  sub_protocol=$2
  opnd_1=$3
  opnd_2=$4
  java -cp out/production/sdis1920-t1g02/ com.feup.sdis.client.Client $peer_ap $sub_protocol $opnd_1 $opnd_2 >> outputs/clients.txt
  sleep 1
}

# start processes
run_client 1 BACKUP t3-cityplan.zip 3
run_client 1 RESTORE t3-cityplan.zip
run_client 2 RESTORE t3-cityplan.zip
#run_client 2 RECLAIM 0
#run_client 2 RECLAIM 10000
#run_client 3 RECLAIM 0
#run_client 1 DELETE t3-cityplan.zip
run_client 1 STATE
run_client 2 STATE
run_client 3 STATE
run_client 4 STATE
run_client 5 STATE