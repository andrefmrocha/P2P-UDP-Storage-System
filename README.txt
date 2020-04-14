Java version: 8

The applications can be executed via 2 different ways:
- Pre-defined scenarios:
  1. Using the script 'run_scenario.sh', where some pre-defined scenarios of execution where automated.
     It starts by compiling and then, according to the given scenario, starts the peers and clients accordingly.
     Running without arguments shows how to use it.

- Manually using the scripts:
  1. Compile the programs using the script 'compile.sh'.
  2. Start the peers using the script 'run_peers.sh'. Running it without arguments tells you how the script can be run.
  3. Send client requests using the 'run_client.sh' script. Again, running without arguments shows how to use it.

- Manually:
  1. Compile using "javac -d out/production/sdis1920-t1g02 $(find . -name "*.java")"
  2. Start each peer using "java -cp out/production/sdis1920-t1g02/ com.feup.sdis.peer.Peer <ID> <ENHANCED>"
     ID is the ID to assign to that peer.
     ENHANCED specifies if the peer should use the ENHANCED protocol. To use it pass 'ENHANCED'.
  3. Start each client using "java -cp out/production/sdis1920-t1g02/ TestApp <PEER_AP> <SUB_PROTOCOL> <OPND_1> <OPND_2>"
     PEER_AP is the peer access point, its ID.
     SUB_PROTOCOL is the protocol to use (BACKUP, RESTORE, DELETE, RECLAIM or STATE).
     OPND_1 is the first operand of the protocol, if applicable.
     OPND_2 is the second operand of the protocol, if applicable.