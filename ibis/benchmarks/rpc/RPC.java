import      java.util.Properties;
import java.io.IOException;

import      ibis.ipl.*;
import      ibis.util.nativeCode.Rdtsc;


class RszHandler implements ResizeHandler {

    java.util.Vector	idents = new java.util.Vector();

    public void join(IbisIdentifier id) {
	synchronized (this) {
	    idents.add(id);
	    notifyAll();
	}
// System.err.println(this + " See join of " + id + "; n := " + idents.size());
    }

    public void leave(IbisIdentifier id) {
	synchronized (this) {
	    idents.remove(id);
	    notifyAll();
	}
// System.err.println(this + " See leave of " + id + "; n := " + idents.size());
    }

    void sync(int n) {
	synchronized (this) {
	    while (idents.size() < n) {
		try {
		    wait();
		} catch (InterruptedException e) {
		}
	    }
	}
    }

}


class RPC implements Upcall, Runnable, ReceivePortConnectUpcall, SendPortConnectUpcall {

    private Ibis        myIbis;
    private Registry    registry;
    private StaticProperties ibisProperties;
    private String	ibis_path;

    private RszHandler	rszHandler;

    private PortType	portType;
    private SendPort    sport;
    private ReceivePort rport;

    private byte[]	byte_buffer;
    private short[]	short_buffer;
    private int[]	int_buffer;
    private long[]	long_buffer;
    private float[]	float_buffer;
    private double[]	double_buffer;
    private Object[]	object_buffer;
    private Object	single_object = null;

    private boolean     upcall = true;
    private boolean     connectUpcalls = false;
    private boolean     one_way = false;
    private final boolean     consume = true;
    private int         count = 1000;
    private int         services = 0;
    private int         size = 0;
    private boolean	cache_write_message = true;
    private WriteMessage writeMessage = null;
    private boolean	finish_upcall_msg = false;
    private boolean	gc_on_rcve = false;

    private int		ncpus = -1;
    private int		rank = -1;

    private int		clients = 1;
    private int		servers = 1;
    private boolean	i_am_client = false;

    private int		warmup = -1;
    private int		first_warmup = 0;

    private int		server_spin = 0;
    private int		client_spin = 0;
    private java.util.Random rand = new java.util.Random();


    private final boolean USE_RESIZEHANDLER = false;
    private final boolean EMPTY_REPLY	= true;

    private boolean reset = true;

    private final int DATA_BYTES   = 0;
    private final int DATA_SHORTS  = DATA_BYTES   + 1;
    private final int DATA_INTS    = DATA_SHORTS  + 1;
    private final int DATA_LONGS   = DATA_INTS    + 1;
    private final int DATA_FLOATS  = DATA_LONGS   + 1;
    private final int DATA_DOUBLES = DATA_FLOATS  + 1;
    private final int DATA_OBJ_1   = DATA_DOUBLES + 1;
    private final int DATA_OBJ_2   = DATA_OBJ_1   + 1;
    private final int DATA_INNER   = DATA_OBJ_2   + 1;
    private final int DATATYPES    = DATA_INNER   + 1;

    private final long	data_size[] =
			    { 1, 2, 4, 8, 4, 8, 4 * 4, 4 * 4, 3 * 4, 0 };

    private int		data_type = DATA_BYTES;

    // private Rdtsc t_send = new Rdtsc();
    // private Rdtsc t_s_finish = new Rdtsc();
    // private Rdtsc t_get_msg = new Rdtsc();
    // private Rdtsc t_rcve = new Rdtsc();
    // private Rdtsc t_r_finish = new Rdtsc();
    // private Rdtsc t_upcall = new Rdtsc();
    // private Rdtsc t_client = new Rdtsc();


    private void send_one(boolean is_server, int size, boolean finish)
	    throws IOException {
	if (! cache_write_message) {
	    writeMessage = sport.newMessage();
	}

	if (! EMPTY_REPLY || ! is_server) {
	    if (single_object != null) {
		// System.err.println("Send single object " + single_object);
		writeMessage.writeObject(single_object);
	    } else if (size > 0) {
		switch (data_type) {
		case DATA_BYTES:
// System.err.println(this + ": writeArray[" + byte_buffer.length + "] writeMessage " + writeMessage);
		    writeMessage.writeArray(byte_buffer);
		    break;
		case DATA_SHORTS:
		    writeMessage.writeArray(short_buffer);
		    break;
		case DATA_INTS:
		    writeMessage.writeArray(int_buffer);
		    break;
		case DATA_LONGS:
		    writeMessage.writeArray(long_buffer);
		    break;
		case DATA_FLOATS:
		    writeMessage.writeArray(float_buffer);
		    break;
		case DATA_DOUBLES:
		    writeMessage.writeArray(double_buffer);
		    break;
		case DATA_OBJ_1:
		case DATA_OBJ_2:
		case DATA_INNER:
		    writeMessage.writeObject(object_buffer);
		    break;
		}
	    }
	}

	// writeMessage.send();
	if (! finish && cache_write_message) {
	    // t_send.start();
	    // writeMessage.reset(false /* don't first send writeMessage */);
	    writeMessage.reset(true /* first send writeMessage */);
	    // t_send.stop();
	} else {
	    // t_send.start();
	    writeMessage.send();
	    // t_send.stop();
	    // t_s_finish.start();
	    writeMessage.finish();
	    // t_s_finish.stop();
	}
// System.err.println("^" + size + " data_type " + data_type + " data " + (byte_buffer == null ? "<nope>" : Integer.toString(byte_buffer.length)));
// System.err.print("^");
    }


    private void send_one(boolean is_server, boolean reset)
	    throws IOException {
	send_one(is_server, size, reset);
    }


    private void send_one(boolean is_server, int size) throws IOException {
	send_one(is_server, size, true);
    }


    private void rcve_one(boolean read_data, int size, ReadMessage m)
	    throws IOException, ClassNotFoundException {
	// t_rcve.start();
	if (! EMPTY_REPLY || read_data) {
	    if (single_object != null) {
		single_object = m.readObject();
		// System.err.println("Receive single_object " + single_object);
	    } else if (size > 0) {
		switch (data_type) {
		case DATA_BYTES:
		    m.readArray(byte_buffer);
		    break;
		case DATA_SHORTS:
		    m.readArray(short_buffer);
		    break;
		case DATA_INTS:
		    m.readArray(int_buffer);
		    break;
		case DATA_LONGS:
		    m.readArray(long_buffer);
		    break;
		case DATA_FLOATS:
		    m.readArray(float_buffer);
		    break;
		case DATA_DOUBLES:
		    m.readArray(double_buffer);
		    break;
		case DATA_OBJ_1:
		case DATA_OBJ_2:
		case DATA_INNER:
		    object_buffer = (Object[])m.readObject();
		    break;
		}
	    }
	}
	// t_rcve.stop();
// System.err.print("v");
// System.err.print(rank + " ");
    }


    private void rcve_one(boolean read_data, int partners, int size)
	    throws IOException, ClassNotFoundException {
	ReadMessage m = null;
	for (int i = 0; i < partners; i++) {
	    // t_get_msg.start();
	    m = rport.receive(m);
	    // t_get_msg.stop();
	    rcve_one(read_data, size, m);
	}
	// t_r_finish.start();
	if (m != null) {
	    m.finish();
	}
	// t_r_finish.stop();
    }


    private void rcve_one(boolean read_data, int partners)
	    throws IOException, ClassNotFoundException {
	rcve_one(read_data, partners, size);
    }


    private void send(int partners, int count)
	    throws IOException, ClassNotFoundException {

	if (count == 0) {
	    return;
	}

	if (cache_write_message) {
	    writeMessage = sport.newMessage();
	    // System.err.println("S-port " + sport + " cached writeMessage = " + writeMessage);
	    // manta.runtime.RuntimeSystem.DebugMe(sport, writeMessage);
	}

	if (one_way) {
	    for (int i = 0; i < count; i++) {
		for (int k = 0; k < client_spin; k++) {
		    int r = rand.nextInt();
		}
		send_one(false /* Not is_server */, i == count - 1);
	    }
	    rcve_one(false /* Not read_data */, partners, 0);
	} else {
	    for (int i = 0; i < count; i++) {
		for (int k = 0; k < client_spin; k++) {
		    int r = rand.nextInt();
		}
		send_one(false /* Not is_server */, i == count - 1);
		rcve_one(false /* Not read_data */, partners);
// System.err.print(".");
	    }
	}
    }


    private void serve(int partners, int count)
	    throws IOException, ClassNotFoundException {

	if (count == 0) {
	    return;
	}

	if (cache_write_message) {
	    writeMessage = sport.newMessage();
	    // System.err.println("S-port " + sport + " cached writeMessage = " + writeMessage);
	    // manta.runtime.RuntimeSystem.DebugMe(sport, writeMessage);
	}

	if (one_way) {
	    for (int i = 0; i < count; i++) {
		rcve_one(true /* read_data */, partners);
		for (int k = 0; k < server_spin; k++) {
		    int r = rand.nextInt();
		}
	    }
	    send_one(true /* is_server */, 0);
	} else {
	    for (int i = 0; i < count; i++) {
// System.err.println("Server wants to receive message " + i + " out of " + count);
		rcve_one(true /* read_data */, partners);
// System.err.print(":");
		for (int k = 0; k < server_spin; k++) {
		    int r = rand.nextInt();
		}
		if (clients == 1 || (i + 1) % clients == 0) {
		    send_one(true /* is_server */, i == count - 1);
		}
		if (gc_on_rcve) {
		    object_buffer = null;
		    System.gc();
		}
	    }
	}
    }


    public void upcall(ReadMessage m) throws IOException {
	try {
	    // t_upcall.start();
	    if (consume) {
		rcve_one(true /* read_data */, size, m);
	    }
	    if (! one_way) {
		if (clients == 1 || (services + 1) % clients == 0) {
		    send_one(true /* is_server */, services == clients * count - 1);
		}
	    }
	    if (gc_on_rcve) {
		object_buffer = null;
		System.gc();
	    }

	    if (finish_upcall_msg) {
		// t_r_finish.start();
		m.finish();
		// t_r_finish.stop();
	    }
// System.err.print("+");

	    // Can we lift the synchronized from the increment?
	    // After all, the implementation will surely do
	    // synchronized()
	    // ???????????????
	    // t_upcall.stop();
	    services++;

	    if (services == first_warmup * clients) {
System.err.println("services " + services + " first_warmup " + first_warmup + " count " + count + " clients " + clients);
		synchronized(this) {
		    first_warmup = 0;
		}
		if (! one_way && cache_write_message) {
		    writeMessage = sport.newMessage();
		    // System.err.println("S-port " + sport + " cached writeMessage = " + writeMessage);
		    // manta.runtime.RuntimeSystem.DebugMe(sport, writeMessage);
System.err.println("Server: seen " + services + " upcall msgs for warmup");
		}
	    }

	    if (services == count * clients) {
		synchronized(this) {
		    notify();
		}
	    }

	    for (int k = 0; k < server_spin; k++) {
		int r = rand.nextInt();
	    }

	/*
	} catch (IOException e) {
	    System.err.println("upcall catches exception " + e);
	    e.printStackTrace();
	*/

	} catch (ClassNotFoundException ec) {
	    System.err.println("upcall catches exception " + ec);
	    System.exit(177);
	}
    }


    private void client() throws IOException, ClassNotFoundException {
	if (warmup > 0) {
	    System.err.println("Do warmup:      " + count + " calls");
	    // warmup
	}
	send(servers, warmup);

	System.err.println("Do measurement: " + count + " calls");

	if (myIbis instanceof ibis.impl.messagePassing.Ibis) {
	    ibis.impl.messagePassing.Ibis.resetStats();
	}
	System.gc();
	// t_client.start();
	long time = System.currentTimeMillis();

	// test
	send(servers, count);

	time = System.currentTimeMillis() - time;
	// t_client.stop();

	double speed = (time * 1000.0) / (double) count;

	System.err.println("Latency: " + count + " calls took " +
			   ((double)time / 1000.0) + " s, time/call = " + speed +
			   " us");
	if (size > 0) {
	    final double MB = 1048576.0;
	    double thrp = (size * data_size[data_type] * count) / MB / (time / 1000.0);

	    if (! EMPTY_REPLY && ! one_way) {
		thrp *= 2;
	    }
	    System.err.println("Throughput " + thrp + " MB/s");
	}
    }


    private void server() throws IOException, ClassNotFoundException {
	System.err.println("Start service, warmup " + warmup + " msgs " + count);
	if (upcall) {

	    synchronized(this) {
		while (services < warmup * clients) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
System.err.println("Server: seen " + services + " msgs for warmup");
		services -= warmup;
	    }
	    if (one_way) {
		send_one(true /* is_server */, 0);
	    }

	    // myIbis.closeWorld();
	    if (myIbis instanceof ibis.impl.messagePassing.Ibis) {
		ibis.impl.messagePassing.Ibis.resetStats();
	    }
	    System.gc();
	    // myIbis.openWorld();

	    // test
	    synchronized(this) {
		while (services < count * clients) {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// ignore
		    }
		}
	    }
System.err.println("Server: seen " + services + " msgs");
	    if (one_way) {
		send_one(true /* is_server */, 0);
	    }

	} else {
	    // warmup
	    serve(clients, warmup);
	    if (myIbis instanceof ibis.impl.messagePassing.Ibis) {
		ibis.impl.messagePassing.Ibis.resetStats();
	    }

	    // test
	    serve(clients, count);
	}

	// I am afraid Net is so fast closing the connection via
	// the ConnectionListener, that we better keep the channel open
	// for a while in the hopes that the client can process our
	// last answer
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // ignore
	}
    }


    private void runClient() throws IOException, ClassNotFoundException {
	System.err.println("I am " + rank + " -- client; clients " + clients + " servers " + servers);

	if (connectUpcalls) {
	    sport = portType.createSendPort("latency-client", (SendPortConnectUpcall)this);
	} else {
	    sport = portType.createSendPort("latency-client");
	}
// manta.runtime.RuntimeSystem.DebugMe(3, 0);

	myIbis.openWorld();
// manta.runtime.RuntimeSystem.DebugMe(4, 0);

	if (connectUpcalls) {
	    rport = portType.createReceivePort("client port " + rank, (ReceivePortConnectUpcall)this);
	} else {
	    rport = portType.createReceivePort("client port " + rank);
	}
	rport.enableConnections();

	for (int i = 0; i < servers; i++) {
	    ReceivePortIdentifier rp;
	    while (true) {
		rp = registry.lookup("server port " + i);
		if (rp != null) {
		    break;
		}
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    // Try again
		}
	    }
	    sport.connect(rp);
	}

	System.err.println(rank + ": client: connected");

	// Do a poor-man's barrier to allow the connections to proceed.
System.err.println(rank + ": Poor-man's barrier receive start...");
	ReadMessage r = rport.receive();
	r.finish();
System.err.println(rank + ": Poor-man's barrier receive finished");

	System.err.println("Go ahead now!");

	client();

	report();

	/* free the send ports first */
	sport.free();
	// System.err.println("Freed my send port");
	rport.free();
	// System.err.println("Freed my rcve port");
    }


    private void runServer() throws IOException, ClassNotFoundException {
	System.err.println("I am " + rank + " -- server; clients " + clients + " servers " + servers);

	if (connectUpcalls) {
	    sport = portType.createSendPort("latency-server", (SendPortConnectUpcall)this);
	} else {
	    sport = portType.createSendPort("latency-server");
	}
// manta.runtime.RuntimeSystem.DebugMe(3, 0);

	myIbis.openWorld();
// manta.runtime.RuntimeSystem.DebugMe(4, 0);

	if (upcall) {
	    if (connectUpcalls) {
		rport = portType.createReceivePort("server port " + (rank - clients), this, (ReceivePortConnectUpcall)this);
	    } else {
		rport = portType.createReceivePort("server port " + (rank - clients), (Upcall)this);
	    }
	} else {
	    if (connectUpcalls) {
		rport = portType.createReceivePort("server port " + (rank - clients), (ReceivePortConnectUpcall)this);
	    } else {
		rport = portType.createReceivePort("server port " + (rank - clients));
	    }
	}
	rport.enableConnections();

	for (int i = 0; i < clients; i++) {
	    ReceivePortIdentifier rp;
	    while (true) {
		rp = registry.lookup("client port " + i);
		if (rp != null) {
		    break;
		}
		try {
		    Thread.sleep(100);
		} catch (InterruptedException e) {
		    // Try again
		}
	    }
System.err.println("Server connects to client #" + i);
	    sport.connect(rp);
	}

	System.err.println("Server: connected");

	// Do a poor-man's barrier to allow the connections to proceed.
	WriteMessage w = sport.newMessage();
System.err.println("Poor-man's barrier send start...");
	w.send();
	w.finish();
System.err.println("Poor-man's barrier send finished");

	System.err.println("Go ahead now!");

	if (upcall) {
	    if (cache_write_message) {
		writeMessage = sport.newMessage();
		// System.err.println("S-port " + sport + " cached writeMessage = " + writeMessage);
		// manta.runtime.RuntimeSystem.DebugMe(sport, writeMessage);
	    }
	    rport.enableUpcalls();
	}
	server();
	if (false && upcall && cache_write_message) {
	    writeMessage.finish();
	}

	report();

	/* free the send ports first */
	sport.free();
	System.err.println("Freed my send port");
	rport.free();
	System.err.println("Freed my rcve port");
    }


    private void parseArgs(String[] args) {
	int         options = 0;

	if (upcall) {
	    System.err.println("****** Upcall server is now default");
	}

	for (int i = 0; i < args.length; i++) {
// System.err.println("Now inspect option[" + i + "] = " + args[i]);
	    if (false) {

	    } else if (args[i].equals("-no-upcall") ||
			args[i].equals("-downcall")) {
		upcall = false;
	    } else if (args[i].equals("-upcall")) {
		upcall = true;

	    } else if (args[i].equals("-one-way")) {
		one_way = true;

	    /* Consume is final:
	    } else if (args[i].equals("-no-consume")) {
		consume = false;
	    } else if (args[i].equals("-consume")) {
		consume = true;
	     * Consume is final */

	    } else if (args[i].equals("-connect-upcall")) {
		connectUpcalls = true;

	    } else if (args[i].equals("-no-connect-upcall")) {
		connectUpcalls = false;

	    } else if (args[i].equals("-gc")) {
		gc_on_rcve = true;

	    } else if (args[i].equals("-cache")) {
		cache_write_message = true;
	    } else if (args[i].equals("-no-cache") ||
			args[i].equals("-send-finish")) {
		cache_write_message = false;

	    } else if (args[i].equals("-servers")) {
		servers = Integer.parseInt(args[++i]);

	    } else if (args[i].equals("-clients")) {
		clients = Integer.parseInt(args[++i]);

	    } else if (args[i].equals("-warmup")) {
		warmup = Integer.parseInt(args[++i]);

	    } else if (args[i].equals("-client-spin")) {
		client_spin = Integer.parseInt(args[++i]);

	    } else if (args[i].equals("-server-spin")) {
		server_spin = Integer.parseInt(args[++i]);

	    } else if (args[i].equals("-finish") ||
			args[i].equals("-upcall-finish")) {
		finish_upcall_msg = true;
	    } else if (args[i].equals("-no-finish") ||
			args[i].equals("-no-upcall-finish")) {
		finish_upcall_msg = false;

	    } else if (args[i].equals("-byte")) {
		data_type = DATA_BYTES;
	    } else if (args[i].equals("-short")) {
		data_type = DATA_SHORTS;
	    } else if (args[i].equals("-int")) {
		data_type = DATA_INTS;
	    } else if (args[i].equals("-long")) {
		data_type = DATA_LONGS;
	    } else if (args[i].equals("-float")) {
		data_type = DATA_FLOATS;
	    } else if (args[i].equals("-double")) {
		data_type = DATA_DOUBLES;
	    } else if (args[i].equals("-obj1")) {
		data_type = DATA_OBJ_1;
	    } else if (args[i].equals("-obj2")) {
		data_type = DATA_OBJ_2;
	    } else if (args[i].equals("-inner")) {
		data_type = DATA_INNER;
	    } else if (args[i].equals("-tree")) {
		data_type = DATA_OBJ_2;

	    } else if (options == 0) {
		count = Integer.parseInt(args[i]);
		options++;

	    } else if (options == 1) {
		size = Integer.parseInt(args[i]);
		options++;

	    } else {
		System.err.println("Unknown option: " + args[i] + " -- abort");
		System.exit(33);
	    }
	}

// System.err.println("finish_upcall_msg = " + finish_upcall_msg);

	if (options < 1) {
	    System.err.println("Usage: Latency <count>");
	    System.exit(33);
	}

	if (warmup == -1) {
	    warmup = count;
	}
	first_warmup = warmup;
    }


    private void parseDataType() {
	if (size == 1 && (data_type == DATA_OBJ_1 || data_type == DATA_OBJ_2 || data_type == DATA_INNER)) {
	    if (data_type == DATA_OBJ_1) {
		single_object = new Data1();
	    } else if (data_type == DATA_OBJ_2) {
		single_object = new Data2(1);
	    } else if (data_type == DATA_INNER) {
		single_object = new WithInner();
	    }
	} else if (size > 0) {

	    if (data_type != DATA_OBJ_1 && data_type != DATA_OBJ_2 && data_type != DATA_INNER) {
		size /= data_size[data_type];
	    }

	    switch (data_type) {
	    case DATA_BYTES:
		byte_buffer = new byte[size];
		break;
	    case DATA_SHORTS:
		short_buffer = new short[size];
		break;
	    case DATA_INTS:
		int_buffer = new int[size];
		break;
	    case DATA_LONGS:
		long_buffer = new long[size];
		break;
	    case DATA_FLOATS:
		float_buffer = new float[size];
		break;
	    case DATA_DOUBLES:
		double_buffer = new double[size];
		break;
	    case DATA_OBJ_1:
		object_buffer = new Data1[size];
		for (int i = 0; i < size; i++) {
		    object_buffer[i] = new Data1();
		}
		break;
	    case DATA_OBJ_2:
		single_object = new Data2(size);
		break;
	    case DATA_INNER:
		object_buffer = new WithInner[size];
		for (int i = 0; i < size; i++) {
		    object_buffer[i] = new WithInner();
		}
		break;
	    }
	}

// manta.runtime.RuntimeSystem.DebugMe(this, byte_buffer);

	if (data_type == DATA_OBJ_2) {
	    // System.err.println(single_object);
	}
    }


    private void parseIbisName() throws IOException, IbisException {
	/* Parse commandline. */

	Properties p = System.getProperties();

	String ser = p.getProperty("ibis.serialization");
	if (ser != null) {
	    ibisProperties.add("Serialization", ser);
	}

	String total = p.getProperty("ibis.pool.total_hosts");
	if (total != null) {
	    ncpus = Integer.parseInt(total);
	    switch (ncpus) {
	    case 0:
		System.err.println("Property " + total + " should be > 0");
		System.exit(41);
		break;
	    default:
		clients = ncpus - 1;
	    }
	} else {
	    /* Try to think of a sensible default */
	    ncpus = 2;
	}
	String my_cpu = p.getProperty("ibis.pool.host_number");
	if (my_cpu != null) {
	    rank = Integer.parseInt(my_cpu);
	}

	String ibis_name = p.getProperty("ibis.name");

	ibis_path = "tcp.TcpIbis";
	String driver = null;

	if (false) {

	} else if (ibis_name != null && ibis_name.equals("mp")) {
	    ibis_path = "messagePassing";

	} else if (ibis_name != null && ibis_name.equals("panda")) {
	    ibis_path = "messagePassing.PandaIbis";

	} else if (ibis_name != null && ibis_name.equals("mpi")) {
	    ibis_path = "messagePassing.MPIIbis";

	} else if (ibis_name != null && ibis_name.startsWith("net.")) {
	    ibis_path = "net.NetIbis";
	    driver = ibis_name.substring("net.".length());

	    String path = "/";
	    if (ser != null && ! ser.equals("none")) {
		String top = "s_" + ser;
System.err.println("Now register static property \"" + (path + ":Driver") + "\" as \"" + top + "\"");
		ibisProperties.add(path + ":Driver", top);
		path = path + top;
	    }
	    while (true) {
		int dot = driver.indexOf('.');
		int end = dot;
		if (end == -1) {
		    end = driver.length();
		}
		String top = driver.substring(0, end);
System.err.println("Now register static property \"" + (path + ":Driver") + "\" as \"" + top + "\"");
		ibisProperties.add(path + ":Driver", top);
		if (dot == -1) {
		    break;
		}
		if (path.equals("/")) {
		    path = path + top;
		} else {
		    path = path + "/" + top;
		}
		driver = driver.substring(dot + 1);
	    }

	} else {
	    ibis_path = "tcp.TcpIbis";
	}

	ibis_path = "ibis.impl." + ibis_path;
	System.err.println("Ibis: " + ibis_path);
    }


    private void createIbis() throws IOException, IbisException {
// manta.runtime.RuntimeSystem.DebugMe(this, byte_buffer);

	if (USE_RESIZEHANDLER) {
	    rszHandler = new RszHandler();
	}

// manta.runtime.RuntimeSystem.DebugMe(-1, 0);

	java.util.Random random = new java.util.Random();
	myIbis = Ibis.createIbis("RPC_" + Math.abs(random.nextInt()), ibis_path, rszHandler);

// manta.runtime.RuntimeSystem.DebugMe(0, 0);
	// myIbis.init();

	Runtime.getRuntime().addShutdownHook(new Thread() {
	    public void run() {

		try {
		    myIbis.end();
		} catch (IOException e) {
		    // Too late to do anything about it
		}
		System.err.println("Ended Ibis");
	    }
	});

	portType = myIbis.createPortType("test type", ibisProperties);
// manta.runtime.RuntimeSystem.DebugMe(2, 0);
    }


    private void registerIbis() throws IOException, IbisException {
	registry = myIbis.registry();
// manta.runtime.RuntimeSystem.DebugMe(1, 0);
    }


    public void run() {

	try {

	    if (rank != -1) {

	    } else if (USE_RESIZEHANDLER) {
		rszHandler.sync(clients + servers);
		rank = rszHandler.idents.indexOf(myIbis.identifier());

	    } else {
		System.err.println("Going to find out my rank, my ID is " + myIbis.identifier().toString() + " name = " + myIbis.identifier().name());

		IbisIdentifier master = (IbisIdentifier)registry.elect("RPC", myIbis.identifier());
// System.err.println("Election master id=" + master + " name=" + master.name());
// System.err.println("Election contender = " + myIbis + " id=" + myIbis.identifier() + " name=" + myIbis.identifier().name());
// manta.runtime.RuntimeSystem.DebugMe(5, 0);
		if (master.equals(myIbis.identifier())) {
// System.err.println("YES--------- I'm the winner, master = " + master + "; me " + myIbis.identifier());
		    rank = 0;
		} else {
		    rank = 1;
		}
	    }

	    if (! i_am_client) {
		runServer();
	    } else {
		runClient();
	    }
System.err.println(this + ": call it quits...; I am " + (i_am_client ? "" : "not ") + "a client\n");
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		// Try again
	    }

	} catch(IOException e) {
	    System.out.println(this + ": Got exception " + e);
	    System.out.println("StackTrace:");
	    e.printStackTrace();

	} catch (ClassNotFoundException e) {
	    System.out.println(this + ": Got exception " + e);
	    System.out.println("StackTrace:");
	    e.printStackTrace();
	}
    }


    private void report() {
	// System.err.println("Timer: t_send     " + t_send.totalTime() + " / " + t_send.nrTimes() + " = " + t_send.averageTimeVal());
	// System.err.println("Timer: t_s_finish " + t_s_finish.totalTime() + " / " + t_s_finish.nrTimes() + " = " + t_s_finish.averageTimeVal());
	// System.err.println("Timer: t_get_msg  " + t_get_msg.totalTime() + " / " + t_get_msg.nrTimes() + " = " + t_get_msg.averageTimeVal());
	// System.err.println("Timer: t_rcve     " + t_rcve.totalTime() + " / " + t_rcve.nrTimes() + " = " + t_rcve.averageTimeVal());
	// System.err.println("Timer: t_r_finish " + t_r_finish.totalTime() + " / " + t_r_finish.nrTimes() + " = " + t_r_finish.averageTimeVal());
	// System.err.println("Timer: t_upcall   " + t_upcall.totalTime() + " / " + t_upcall.nrTimes() + " = " + t_upcall.averageTimeVal());
	// System.err.println("Timer: t_client   " + t_client.totalTime() + " / " + t_client.nrTimes() + " = " + t_client.averageTimeVal());
    }


    RPC(String[] args, RPC client) {
	try {

	    ibisProperties = new StaticProperties();

	    parseArgs(args);
	    parseIbisName();
	    parseDataType();

	    if (client == null) {
		createIbis();
		// ibis.ipl.Timer timer = Ibis.newTimer("ibis.util.nativeCode.Rdtsc");
		// timer.start();
		registerIbis();
		if (ncpus == 1) {
		    RPC dl = new RPC(args, this);
		    Thread server = new Thread(dl);
		    server.setName("RPC server");
		    server.start();
		    clients = 1;
		    i_am_client = true;
		    System.err.println("Kick-force server run; my rank " + rank + " ncpus " + ncpus + " clients " + clients + " servers " + servers);
		} else {
		    i_am_client = rank < clients;
		}
		System.err.println(this + ": Regular run; rank " + rank + " ncpus " + ncpus + " clients " + clients + " servers " + servers);
		run();
		// timer.stop();
		// System.err.println("Nano timering -> " + timer.totalTimeVal());
		System.exit(0);
	    } else {
		i_am_client = false;
		rank = 1;
		clients = 1;
		System.err.println(this + ": Forced server run; rank " + rank + " ncpus " + ncpus + " clients " + clients + " servers " + servers);
		myIbis = client.myIbis;
		this.rszHandler = client.rszHandler;
		this.portType = client.portType;
		registerIbis();
	    }

	} catch (IOException e) {
	    System.err.println("Top-level exception " + e);

	} catch(IbisException e) {
	    System.out.println("Got exception " + e);
	    System.out.println("StackTrace:");
	    e.printStackTrace();
	}
    }


    RPC(String[] args) {
	this(args, null);
    }


    /**
     * Interface ReceivePortConnectUpcall
     */
    public boolean gotConnection(ReceivePort rp, SendPortIdentifier sp) {
	System.err.println("Got connection with send port " + sp);
	return true;
    }

    public void lostConnection(ReceivePort rp, SendPortIdentifier sp, Exception e) {
	System.err.println("Lost connection with send port " + sp
			    + " threw " + e);
    }


    /**
     * Interface SendPortConnectUpcall
     */
    public void lostConnection(SendPort sp, ReceivePortIdentifier rp, Exception e) {
	System.err.println("Lost connection with receive port " + rp
			    + " threw " + e);
    }


    public static void main(String[]args) {
	new RPC(args);
    }
}