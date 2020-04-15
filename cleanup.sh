#!/bin/bash

# print usage
if [ "$#" != 1 ]; then
  echo "Usage: $0 PEER_ID"
  echo "  PEER_ID is the ID of the peer that should have its files cleared"
  exit 1
fi

peer_folder="peer-$1"
rm -rf peers/$peer_folder &> /dev/null