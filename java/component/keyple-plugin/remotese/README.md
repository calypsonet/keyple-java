#Remote Secure Element Plugin

The Remote SE Plugin allows a terminal to communicate with a "remote" Se Reader plugged into a another terminal. 

In a Calypso context, it is useful when your SAM reader is and your PO reader are not connected to the same terminal. With the remote se plugin, you can open Calypso transaction within a distributed architecture.

##How it works

A terminal (let's identify it terminal1) wants to communicate with another terminals reader (terminal2). To give access to it's native reader, terminal2 should open a session to terminal1 via ``nativeReaderService#connect()`` method. Doing this, terminal1 receives control of the reader. terminal2 is identified as the slave and terminal1 as the master.

When terminal2 (slave) opens sucessfully a session to terminal1 (master), a ``VirtualReader`` is created on terminal1' side. This ``VirtualReader`` is viewed as a local reader for the master, in fact it acts as a proxy to the ``SeReader`` on terminal2.


##Network configuration

Usually distributed architecture will rely on a TCP/IP network to communicate. It is up to the users to choose which protocol to use on top of it. The remote se plugin does not provide the network implementation, but it provides a set of interfaces to be implemented

Examples of implementation can be found in example/calypso/remotese/transport

##Transport implementation

###KeypleDto

###DtoHandler

###DtoSender

###TransportNode


