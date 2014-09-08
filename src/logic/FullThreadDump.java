package logic;
/*
 * @(#)FullThreadDump.java  1.5 05/11/17
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)FullThreadDump.java  1.5 05/11/17
 */

import static java.lang.management.ManagementFactory.THREAD_MXBEAN_NAME;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import static java.lang.management.ManagementFactory.newPlatformMXBeanProxy;

import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import javax.swing.JFrame;

import UI.ToolGui;

/**
 * This FullThreadDump class demonstrates the capability to get a full thread
 * dump and also detect deadlock remotely.
 */
public class FullThreadDump extends MBean implements Runnable {
	private ToolGui frame;
	private Writer writer;
	private File dumpFile;
  //private MBeanServerConnection server;

  //private JMXConnector jmxc;
  
  public FullThreadDump(MBeanServerConnection mBeanConnection, int initDelay, int delay, int subsequentTimes, JFrame frame) {
	  super(mBeanConnection, initDelay, delay, subsequentTimes, frame);
	  this.frame = (ToolGui)frame;
	  
	  dumpFile = new File(".", "threadDump." + System.currentTimeMillis() + ".log");
	  try {writer = new BufferedWriter(new FileWriter(dumpFile));}catch(IOException e){System.out.println(e);}
	  
  }

  public void dump() {
    try {
      ThreadMonitor monitor = new ThreadMonitor(mBeanConnection);
      monitor.threadDump(writer);
      if (!monitor.findDeadlock()) {
    	String print = "No deadlock found.";
        System.out.println(print);
        try {writer.write(print+"\n\n"); } catch (IOException e) { System.out.println(e); }
      }
    } catch (IOException e) {
      System.err.println("\nCommunication error: " + e.getMessage());
      System.exit(1);
    }
  }
  
	/**
	 * Creates a number of thread dumps based on delay settings
	 * @throws Exception 
	 */
	@Override
	public void run() {
		try {			
			Thread.sleep(initDelay);
			dump();
			//System.out.println(subsequentTimes);
			for (int i=0;i<subsequentTimes;i++) {
				Thread.sleep(delay);
				dump();
			}		
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try { writer.close(); } catch(IOException e){}
			frame.threadThreadDone();
		}
	}

  /**
   * Connect to a JMX agent of a given URL.
   */
  /*private void connect(String urlPath) {
    try {
      JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
      this.jmxc = JMXConnectorFactory.connect(url);
      this.server = jmxc.getMBeanServerConnection();
    } catch (MalformedURLException e) {
      // should not reach here
    } catch (IOException e) {
      System.err.println("\nCommunication error: " + e.getMessage());
      System.exit(1);
    }
  }*/
  
  /*public FullThreadDump(String hostname, int port) {
  System.out.println("Connecting to " + hostname + ":" + port);

  // Create an RMI connector client and connect it to
  // the RMI connector server
  String urlPath = "/jndi/rmi://" + hostname + ":" + port + "/jmxrmi";
  connect(urlPath);
}*/

  /*public static void main(String[] args) {
    if (args.length != 1) {
      usage();
    }

    String[] arg2 = args[0].split(":");
    if (arg2.length != 2) {
      usage();
    }
    String hostname = arg2[0];
    int port = -1;
    try {
      port = Integer.parseInt(arg2[1]);
    } catch (NumberFormatException x) {
      usage();
    }
    if (port < 0) {
      usage();
    }

    // get full thread dump and perform deadlock detection
    FullThreadDump ftd = new FullThreadDump(hostname, port);
    ftd.dump();
  }

  private static void usage() {
    System.out.println("Usage: java FullThreadDump <hostname>:<port>");
  }*/
}

/*
 * @(#)ThreadMonitor.java 1.6 05/12/22
 * 
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN") AND ITS
 * LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

/*
 * @(#)ThreadMonitor.java 1.6 05/12/22
 */

/**
 * Example of using the java.lang.management API to dump stack trace and to
 * perform deadlock detection.
 * 
 * @author Mandy Chung
 * @version %% 12/22/05
 */
class ThreadMonitor {
  private MBeanServerConnection server;

  private ThreadMXBean tmbean;

  private ObjectName objname;

  // default - JDK 6+ VM
  private String findDeadlocksMethodName = "findDeadlockedThreads";

  private boolean canDumpLocks = true;
  
  private Writer writer;
  
  private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

  /**
   * Constructs a ThreadMonitor object to get thread information in a remote
   * JVM.
   */
  public ThreadMonitor(MBeanServerConnection server) throws IOException {
    this.server = server;
    this.tmbean = newPlatformMXBeanProxy(server, THREAD_MXBEAN_NAME, ThreadMXBean.class);
    try {
      objname = new ObjectName(THREAD_MXBEAN_NAME);
    } catch (MalformedObjectNameException e) {
      // should not reach here
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    }
    parseMBeanInfo();
  }

  /**
   * Constructs a ThreadMonitor object to get thread information in the local
   * JVM.
   */
  public ThreadMonitor() {
    this.tmbean = getThreadMXBean();
  }

  /**
   * Prints the thread dump information to System.out.
   */
  public void threadDump(Writer writer) {
	  this.writer=writer;
	  //private MBeanServ
    if (canDumpLocks) {
      if (tmbean.isObjectMonitorUsageSupported() && tmbean.isSynchronizerUsageSupported()) {
        // Print lock info if both object monitor usage
        // and synchronizer usage are supported.
        // This sample code can be modified to handle if
        // either monitor usage or synchronizer usage is supported.
        dumpThreadInfoWithLocks();
      }
    } else {
      dumpThreadInfo();
    }
  }

  private void dumpThreadInfo() {
	Date date = new Date();
	String print = dateFormat.format(date) + " - Full Java thread dump";
    System.out.println(print);
    try {writer.write(print+"\n");} catch (IOException e) {System.out.println(e);}
    
    long[] tids = tmbean.getAllThreadIds();
    ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
    for (ThreadInfo ti : tinfos) {
      printThreadInfo(ti);
    }
  }

  /**
   * Prints the thread dump information with locks info to System.out.
   */
  private void dumpThreadInfoWithLocks() {
	Date date = new Date();
	String print = dateFormat.format(date) + " - Full Java thread dump";
    System.out.println(print);
    try {writer.write(print+"\n");} catch (IOException e) {System.out.println(e);}
    
    ThreadInfo[] tinfos = tmbean.dumpAllThreads(true, true);
    for (ThreadInfo ti : tinfos) {
      printThreadInfo(ti);
      LockInfo[] syncs = ti.getLockedSynchronizers();
      printLockInfo(syncs);
    }
    System.out.println();
    try { writer.write("\n"); } catch (IOException e) { System.out.println(e); }
  }

  private static String INDENT = "    ";

  private void printThreadInfo(ThreadInfo ti) {
    // print thread information
    printThread(ti);

    // print stack trace with locks
    StackTraceElement[] stacktrace = ti.getStackTrace();
    MonitorInfo[] monitors = ti.getLockedMonitors();
    for (int i = 0; i < stacktrace.length; i++) {
      StackTraceElement ste = stacktrace[i];
      
      String print = INDENT + "at " + ste.toString();
      System.out.println(print);

      try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
      
      for (MonitorInfo mi : monitors) {
        if (mi.getLockedStackDepth() == i) {
          print = INDENT + "  - locked " + mi;
          System.out.println(print);      
          try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
        }
      }
    }
    System.out.println();
    try { writer.write("\n"); } catch (IOException e) { System.out.println(e); }
  }

  private void printThread(ThreadInfo ti) {
    StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" + " Id="
        + ti.getThreadId() + " in " + ti.getThreadState());
    if (ti.getLockName() != null) {
      sb.append(" on lock=" + ti.getLockName());
    }
    if (ti.isSuspended()) {
      sb.append(" (suspended)");
    }
    if (ti.isInNative()) {
      sb.append(" (running in native)");
    }
    String print = sb.toString();
    System.out.println(print);
    try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
    
    if (ti.getLockOwnerName() != null) {
      print = INDENT + " owned by " + ti.getLockOwnerName() + " Id=" + ti.getLockOwnerId();
      System.out.println(print);
      try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
    }
  }

  private void printMonitorInfo(ThreadInfo ti, MonitorInfo[] monitors) {
	String print = INDENT + "Locked monitors: count = " + monitors.length;
    System.out.println(print);
    try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
    
    for (MonitorInfo mi : monitors) {
      print=INDENT + "  - " + mi + " locked at ";
      System.out.println(print);
      try { writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
      
      print=INDENT + "      " + mi.getLockedStackDepth() + " "+ mi.getLockedStackFrame();
      System.out.println(print);
      try {writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
    }
  }

  private void printLockInfo(LockInfo[] locks) {
	String print = INDENT + "Locked synchronizers: count = " + locks.length;
    System.out.println(print);
    try {writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
    
    for (LockInfo li : locks) {
      print = INDENT + "  - " + li;
      System.out.println(print);
      try {writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
      
    }
    System.out.println();
    try {writer.write("\n"); } catch (IOException e) { System.out.println(e); }
  }

  /**
   * Checks if any threads are deadlocked. If any, print the thread dump
   * information.
   */
  public boolean findDeadlock() {
    long[] tids;
    if (findDeadlocksMethodName.equals("findDeadlockedThreads")
        && tmbean.isSynchronizerUsageSupported()) {
      tids = tmbean.findDeadlockedThreads();
      if (tids == null) {
        return false;
      }
      
      String print = "Deadlock found :-";
      System.out.println(print);
      try {writer.write(print+"\n"); } catch (IOException e) { System.out.println(e); }
      ThreadInfo[] infos = tmbean.getThreadInfo(tids, true, true);
      for (ThreadInfo ti : infos) {
        printThreadInfo(ti);
        printLockInfo(ti.getLockedSynchronizers());
        
        System.out.println();
        try {writer.write("\n"); } catch (IOException e) { System.out.println(e); }
      }
    } else {
      tids = tmbean.findMonitorDeadlockedThreads();
      if (tids == null) {
        return false;
      }
      ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
      for (ThreadInfo ti : infos) {
        // print thread information
        printThreadInfo(ti);
      }
    }

    return true;
  }

  private void parseMBeanInfo() throws IOException {
    try {
      MBeanOperationInfo[] mopis = server.getMBeanInfo(objname).getOperations();

      // look for findDeadlockedThreads operations;
      boolean found = false;
      for (MBeanOperationInfo op : mopis) {
        if (op.getName().equals(findDeadlocksMethodName)) {
          found = true;
          break;
        }
      }
      if (!found) {
        // if findDeadlockedThreads operation doesn't exist,
        // the target VM is running on JDK 5 and details about
        // synchronizers and locks cannot be dumped.
        findDeadlocksMethodName = "findMonitorDeadlockedThreads";
        canDumpLocks = false;
      }
    } catch (IntrospectionException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    } catch (InstanceNotFoundException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    } catch (ReflectionException e) {
      InternalError ie = new InternalError(e.getMessage());
      ie.initCause(e);
      throw ie;
    }
  }
}