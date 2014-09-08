package logic;

import javax.management.MBeanServerConnection;
import javax.swing.JFrame;

import UI.ToolGui;

public class MBean {
	protected MBeanServerConnection mBeanConnection;
	protected int initDelay;
	protected int delay;
	protected int subsequentTimes;
	
	protected ToolGui frame;
	
	public MBean(MBeanServerConnection mBeanConnection, int initDelay, int delay, int subsequentTimes, JFrame frame) {
		this.mBeanConnection = mBeanConnection;
		this.initDelay = initDelay;
		this.delay = delay;
		this.subsequentTimes = subsequentTimes;
		this.frame = (ToolGui)frame;
	}
}
