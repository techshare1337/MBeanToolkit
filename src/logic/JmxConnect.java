package logic;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxConnect {
	static MBeanServerConnection mBeanConnection;
	static JMXConnector jmxConnector;
	/**
	 * Connect to the server
	 * @throws IOException 
	 */
	public static MBeanServerConnection connectToServer(String serverName) throws IOException {
		String url = "service:jmx:rmi:///jndi/rmi://" + serverName + "/jmxrmi";
		JMXServiceURL jmxURL = new JMXServiceURL(url);
		jmxConnector = JMXConnectorFactory.connect(jmxURL);
		return jmxConnector.getMBeanServerConnection();
	}
	
	/**
     * Close the connection
     */
	public static void closeConnection() {
		if (jmxConnector != null) {
			try {
				jmxConnector.close();
				System.out.println("connection closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
