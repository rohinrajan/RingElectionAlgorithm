# RingElectionAlgorithm
This project is an simple implementation of an ring election algorithm using multiple sockets and threads.

# Approach
Using static nodes to be used to connect with each other and begin immediately to elect the leader for communication.
Using token for ensuring connection between nodes and in case if there is any disconnection then it would reconnect in order to maintain interconnection of nodes. 
In this we are using tcp sockets running of on a pool of port numbers if any node which was previously down has got back up it would be able to reconnect to the existing topology.

# Issues
1.  Static nodes are generated with a max of 8 nodes in order has to be maintained
2.  Prev tokens sometimes causes corruption of the routing table for the other nodes.

# Future Enhancements
1. Provide a broadcast way for dynamic allocation of nodes during setup of topology
2. Improve logging and display of data
3. GUI for each node representation.
