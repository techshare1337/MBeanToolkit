package logic;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.swing.JFrame;

public class HeapDump extends MBean implements Runnable {	
	private String heapDumpName;
	
	public HeapDump(MBeanServerConnection mBeanConnection, String heapDumpName, int initDelay, int delay, int subsequentTimes, JFrame frame) {
		super(mBeanConnection, initDelay, delay, subsequentTimes, frame);
		this.heapDumpName = heapDumpName;	
	}

	/**
	 * Create heap dump with specified filename
	 * @throws Exception 
	 */
	public void dumpHeap(String heapDumpName) {
		Object[] params = new Object[] { heapDumpName, Boolean.TRUE };
		String[] signature = new String[] { String.class.getName(), boolean.class.getName() };
			
		try {
			ObjectName mbeanName = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
            mBeanConnection.invoke(mbeanName, "dumpHeap", params, signature);
            System.out.println("*** Heap dumped to: " + heapDumpName);
        } catch(Exception e) {
            System.out.println("Heap dump attempt failed: " + e);
        }       
	}

	/**
	 * Creates a number of heap dumps based on delay settings
	 * @throws Exception 
	 */
	@Override
	public void run() {
		try {		
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			
			String name = heapDumpName;
			String extension = "";
			int dotIndex = heapDumpName.lastIndexOf('.');
			if (dotIndex != -1) {
				name = heapDumpName.substring(0,dotIndex);
				extension = heapDumpName.substring(dotIndex);
			}
			
			Thread.sleep(initDelay);
			dumpHeap(name + "_" + formatter.format(new Date()) + extension);			
			
			for (int i=0;i<subsequentTimes;i++) {
				Thread.sleep(delay);
				dumpHeap(name + "_" + formatter.format(new Date()) + extension);
			}		
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {	
			System.out.println("heap thread completed");
			frame.heapThreadDone();
		}
	}
	
	
	
	
	/**
	 * Create heap dump with specified filename (2nd approach)
	 * @throws Exception 
	 */
	/*public static void dumpHeap2(String heapDumpName) {
		try {
			String jvmPid = (String) mBeanConnection.getAttribute(new ObjectName("java.lang:type=Runtime"), "Name");
	        String pid = jvmPid.substring(0, jvmPid.lastIndexOf('@'));
	        System.out.printf("jvmPid = '%s', pid = '%s'\n", jvmPid, pid);
	        String javaHome = System.getenv("JAVA_HOME");
	        System.out.println("using JAVA_HOME from environment: '" + javaHome + "'");
	        String command = javaHome + "/bin/jmap -dump:format=b,file=" + heapDumpName + " " + pid;
	        System.out.println("running command: " + command);
	        Process p = Runtime.getRuntime().exec(command);
	        System.out.println("return code: " + p.waitFor());
		} catch (Exception e) {
			System.out.println("Heap dump attempt failed: " + e);
		}
	}*/
	
}
