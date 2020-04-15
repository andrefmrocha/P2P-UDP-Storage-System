#!/bin/bash

usage() {
  echo "Usage: $0 PEER_AP SUB_PROTOCOL <OPND_1> <OPND_2>" >&2
  echo "  PEER_AP is the peer access point"
  echo "  SUB_PROTOCOL is the protocol to use"
  echo "  OPND_1 is an optional first operand"
  echo "  OPND_2 is an optional second operand"
}

# print usage
if [ "$#" -lt 2 -o "$#" -gt 4 ]; then
  usage
  exit 1
fi

argc=$#
oper=$2

# Validate remaining arguments 
case $oper in
BACKUP)
	if(( argc != 4 )) 
	then
		echo "Usage: $0 <peer_ap> BACKUP <filename> <rep degree>"
		exit 1
	fi
	;;
RESTORE)
	if(( argc != 3 ))
	then
		echo "Usage: $0 <peer_app> RESTORE <filename>"
    exit 1
	fi
	;;
DELETE)
	if(( argc != 3 ))
	then
		echo "Usage: $0 <peer_app> DELETE <filename>"
		exit 1
	fi
	;;
RECLAIM)
	if(( argc != 3 ))
	then
		echo "Usage: $0 <peer_app> RECLAIM <max space>"
		exit 1
	fi
	;;
STATE)
	if(( argc != 2 ))
	then
		echo "Usage: $0 <peer_app> STATE"
		exit 1
	fi
	;;
*)
  usage
	exit 1
	;;
esac


peer_ap=$1
sub_protocol=$2
opnd_1=$3
opnd_2=$4
java -cp build/ TestApp $peer_ap $sub_protocol $opnd_1 $opnd_2