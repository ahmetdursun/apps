import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.io.IOException;
import java.net.InetAddress;

public class RMI_init {

	static Registry reg = null;
	static boolean isInitialized = false;

	public static Registry getRegistry(String registryOwner) 
		throws IOException {

		if (isInitialized) {
			System.out.println("Registry already initialized");
			return reg;
		}
		isInitialized = true;

		String version = System.getProperty("java.version");
		if (version == null || version.startsWith("1.1")) {
			// Start a security manager.
			// System.out.println("Start a security manager");
			System.setSecurityManager(new RMISecurityManager());
		}

		int port = Registry.REGISTRY_PORT;

		InetAddress[] reg_names = InetAddress.getAllByName(registryOwner);
		int i;
		for (i = 0; i < reg_names.length; i++) {
// System.out.println("Compare hosts " + InetAddress.getLocalHost() + " and " + reg_names[i]); System.out.flush();
			if (reg_names[i].equals(InetAddress.getLocalHost())) {
				break;
			}
		}
		if (i != reg_names.length) {
			try {
				reg = LocateRegistry.createRegistry(port);
				System.out.println("Registry is " + reg);
			
				return reg;
			
			} catch (Exception e) {}
		} 

		while (reg == null) {
			try {
				reg = LocateRegistry.getRegistry(registryOwner, port);
			} catch (java.rmi.RemoteException e) {
				try {
					System.out.println("Look up registry: sleep a while..");
					Thread.sleep(500);
				} catch (InterruptedException eI) {
				}
			}
		}

		System.out.println("Registry is " + reg);

		return reg;
	}
}