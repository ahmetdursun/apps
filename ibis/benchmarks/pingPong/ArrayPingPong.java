/* $Id$ */


import ibis.ipl.*;

import java.util.Properties;
import java.util.Random;

import java.io.IOException;

class ArrayPingPong implements PredefinedCapabilities {
static class Sender {
    SendPort sport;
    ReceivePort rport;

    Sender(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void send(int count, int repeat, int arraySize) throws Exception {
	double[] a = new double[arraySize];

        for (int r = 0; r < repeat; r++) {

            long time = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeArray(a);
                writeMessage.finish();

                ReadMessage readMessage = rport.receive();
		readMessage.readArray(a);
                readMessage.finish();
            }

            time = System.currentTimeMillis() - time;

            double speed = (time * 1000.0) / (double) count;
	    double tp = ((count * arraySize * 8) / (1024*1024)) / (time / 1000.0);

            System.err.println(count + " calls took "
                    + (time / 1000.0) + " s, time/call = " + speed
                    + " us, throughput = " + tp + " MB/s, msg size = " + (arraySize * 8));
        }
    }
}

static class ExplicitReceiver {

    SendPort sport;

    ReceivePort rport;

    ExplicitReceiver(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void receive(int count, int repeat, int arraySize) throws Exception {

	double[] a = new double[arraySize];

        for (int r = 0; r < repeat; r++) {
            for (int i = 0; i < count; i++) {
                ReadMessage readMessage = rport.receive();
		readMessage.readArray(a);
                readMessage.finish();

                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeArray(a);
                writeMessage.finish();
            }
        }
    }
}


    static Ibis ibis;

    static Registry registry;

    public static void main(String[] args) {
        int count = 1000;
        int repeat = 10;
        int rank;

        try {
            CapabilitySet s = new CapabilitySet(
                    WORLDMODEL_CLOSED, SERIALIZATION_OBJECT,
                    CONNECTION_ONE_TO_ONE,
                    COMMUNICATION_RELIABLE, RECEIVE_EXPLICIT);
            ibis = IbisFactory.createIbis(s, null, null, null);

            registry = ibis.registry();

            PortType t = ibis.createPortType(s);

            SendPort sport = t.createSendPort("send port");
            ReceivePort rport;

            IbisIdentifier master = registry.elect("latency");
            IbisIdentifier remote;

            if (master.equals(ibis.identifier())) {
                rank = 0;
                remote = registry.getElectionResult("client");
            } else {
                registry.elect("client");
                rank = 1;
                remote = master;
            }

	    Sender sender = null;
	    ExplicitReceiver receiver = null;
            if (rank == 0) {
                rport = t.createReceivePort("test port");
                rport.enableConnections();
                sport.connect(remote, "test port");
                sender = new Sender(rport, sport);
            } else {
                sport.connect(remote, "test port");
                rport = t.createReceivePort("test port");
                rport.enableConnections();
                receiver = new ExplicitReceiver(rport, sport);
            }

	    int dataSize = 1;
	    for(int i=0; i<20; i++) {
		if (rank == 0) {
                    sender.send(count, repeat, dataSize);
		} else {
                    receiver.receive(count, repeat, dataSize);
		}

		dataSize *= 2;

		if(i >= 12) count /= 2;
	    }

            /* free the send ports first */
            sport.close();
            rport.close();
            ibis.end();
        } catch (Exception e) {
            System.err.println("Got exception " + e);
            System.err.println("StackTrace:");
            e.printStackTrace();
        }
    }
}
