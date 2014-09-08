package UI;
import logic.*;

import java.awt.*;
import java.awt.event.*;

import javax.management.MBeanServerConnection;
import javax.swing.*;

import logic.JmxConnect;

@SuppressWarnings("serial")
public class ToolGui extends JFrame implements ItemListener{
	final JFrame frame = this;
	JFrame connect;
	MBeanServerConnection mBeanConnection;

	JButton heapBtn;
	final JButton heapCancelBtn;
	
	JButton threadBtn;
	final JButton threadCancelBtn;
	
	private Thread heapThread;
	private Thread threadThread;
	
	final JCheckBox hScheduleChkBox;
	final JCheckBox tScheduleChkBox;
	
	final JLabel heapStartDelayLbl;
	final JTextField heapStartDelayField;
	final JLabel heapRepeatLbl;
	final JTextField heapRepeatField;
	final JLabel heapForLabel;
	final JTextField heapForField;
	
	final JLabel threadStartDelayLbl;
	final JTextField threadStartDelayField;
	final JLabel threadRepeatLabel;
	final JTextField threadRepeatField;
	final JLabel threadForLabel;
	final JTextField threadForField;
	
	JLabel secondsLabel;
	JLabel secondsLabel2;
	
	public ToolGui(MBeanServerConnection mBeanConnection, final JFrame connect, String connectionString) {
		this(connectionString);		
		this.mBeanConnection = mBeanConnection;
		this.connect = connect;
	}

	public ToolGui(String connectionString) {
		super("MBean Toolkit: Operation invocation - "+connectionString);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		// main operation invocation panel
        JPanel operationPanel = new JPanel();
        operationPanel.setLayout(new GridLayout(0,2));
        operationPanel.setBorder(BorderFactory.createTitledBorder("Operation invocation"));
        
        // heap dump panel
        JPanel heapDumpPanel = new JPanel();
        heapDumpPanel.setLayout(new FlowLayout());
        
        JLabel heapNameLbl = new JLabel("Heap Dump Filename:");
        heapDumpPanel.add(heapNameLbl);
        
        final JTextField heapNameField = new JTextField();
        heapNameField.setPreferredSize(new Dimension(150,20));
        heapDumpPanel.add(heapNameField);
        
        heapBtn = new JButton("Heap Dump");
        heapDumpPanel.add(heapBtn);
        
        heapCancelBtn = new JButton("Cancel");
        heapCancelBtn.setEnabled(false);
        heapDumpPanel.add(heapCancelBtn);
        
        operationPanel.add(heapDumpPanel);        
        
        // heap scheduler panel
        JPanel heapSchedulerPanel = new JPanel();       
        hScheduleChkBox = new JCheckBox("Scheduler");    
        heapStartDelayLbl = new JLabel("start delay:");  
        heapStartDelayField = new JTextField();
        heapRepeatLbl = new JLabel("Repeat every:");
        heapRepeatField = new JTextField();
        heapForLabel = new JLabel("for a duration of:");
        heapForField = new JTextField();
        secondsLabel = new JLabel("(seconds)");
        createSchedulePanels(heapSchedulerPanel, hScheduleChkBox, heapStartDelayLbl, heapStartDelayField,
    			heapRepeatLbl, heapRepeatField, heapForLabel, heapForField, secondsLabel, operationPanel);
        
        // thread dump panel
        JPanel threadDumpPanel = new JPanel();
        threadDumpPanel.setLayout(new FlowLayout());
        
        threadBtn = new JButton("Thread Dump");
        threadDumpPanel.add(threadBtn);
        
        threadCancelBtn = new JButton("Cancel");
        threadCancelBtn.setEnabled(false);
        threadDumpPanel.add(threadCancelBtn);
        
        operationPanel.add(threadDumpPanel);
        
        // thread dump scheduler panel
        JPanel threadSchedulerPanel = new JPanel();  
        tScheduleChkBox = new JCheckBox("Scheduler");       
        threadStartDelayLbl = new JLabel("start delay:");        
        threadStartDelayField = new JTextField();   
        threadRepeatLabel = new JLabel("Repeat every:");
        threadRepeatField = new JTextField();
        threadForLabel = new JLabel("for a duration of:");
        threadForField = new JTextField();
        secondsLabel2 = new JLabel("(seconds)");
        createSchedulePanels(threadSchedulerPanel, tScheduleChkBox, threadStartDelayLbl, threadStartDelayField,
        		threadRepeatLabel, threadRepeatField, threadForLabel, threadForField, secondsLabel2, operationPanel);
        
        // bottom panel
        JLabel instructions = new JLabel("Schedule units should be entered in seconds");
        operationPanel.add(instructions);
        
        JButton backBtn = new JButton("New Connection");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(operationPanel);
        add(backBtn);
		pack();
        setMinimumSize(new Dimension(600,600));
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setBounds(screenSize.width/2 - frameSize.width/5, screenSize.height/2 - frameSize.height/2, 600, 600);
        setVisible(true);
        
        // heap dump btn
        heapBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {			
				int initDelay = 0;
				int delay = 0;
				int subsequentTimes = 0;
				
				try {
					if (heapStartDelayLbl.isEnabled()) {
						initDelay = Integer.parseInt(heapStartDelayField.getText()) * 1000;
						delay = Integer.parseInt(heapRepeatField.getText()) * 1000;
						if (delay > 0) {
							subsequentTimes = (Integer.parseInt(heapForField.getText()) * 1000) / delay;
						}				
					}
					
					if (initDelay < 0 || delay < 0 || subsequentTimes < 0) {
						JOptionPane.showMessageDialog(frame, "Values can't be negative", "Heap dump error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
					} else {
						HeapDump heapDump = new HeapDump(mBeanConnection, heapNameField.getText(), 
								initDelay, delay, subsequentTimes, frame);			
						heapThread = new Thread(heapDump);
						heapThread.start();
						
						threadStarted(heapBtn, heapCancelBtn);
					}				
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Heap dump error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
		});
        
        // heap dump cancel btn
        heapCancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {			
				heapThread.interrupt();
			}
		});
        
        // thread dump btn
        threadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				int initDelay = 0;
				int delay = 0;
				int subsequentTimes = 0;
				
				try {
					if (threadStartDelayLbl.isEnabled()) {
						initDelay = Integer.parseInt(threadStartDelayField.getText()) * 1000;
						delay = Integer.parseInt(threadRepeatField.getText()) * 1000;
						if (delay > 0) {
							subsequentTimes = (Integer.parseInt(threadForField.getText()) * 1000) / delay;
						}
					}
					
					if (initDelay < 0 || delay < 0 || subsequentTimes < 0) {
						JOptionPane.showMessageDialog(frame, "Values can't be negative", "Thread dump error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
					} else {
						FullThreadDump threadDump = new FullThreadDump(mBeanConnection, initDelay, delay, subsequentTimes, frame);
						threadThread = new Thread(threadDump);
						threadThread.start();
						
						threadStarted(threadBtn, threadCancelBtn);
					}					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Thread dump error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
		});
        
        // thread dump cancel btn
        threadCancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {			
				threadThread.interrupt();
			}
		});   
        
        // back btn
        backBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {				
				connect.setVisible(true);
				frame.dispose();
				JmxConnect.closeConnection();
				
				if (heapThread != null)
					heapThread.interrupt();
				if (threadThread != null)
					threadThread.interrupt();
			}
		});
	}
	
	// create scheduler panels
	private void createSchedulePanels(JPanel schedulerPanel, JCheckBox scheduleChkBox, JLabel startDelayLbl, JTextField startDelayField,
			JLabel repeatLbl, JTextField repeatField, JLabel forLabel, JTextField forField, JLabel secondsLabel, JPanel operationPanel) {
        
        scheduleChkBox.addItemListener(this);
        schedulerPanel.add(scheduleChkBox);
        
        schedulerPanel.add(startDelayLbl);
        
        startDelayField.setPreferredSize(new Dimension(100,20));
        schedulerPanel.add(startDelayField);
        
        schedulerPanel.add(repeatLbl);
        
        repeatField.setPreferredSize(new Dimension(100,20));
        schedulerPanel.add(repeatField);
        
        schedulerPanel.add(forLabel);
        
        forField.setPreferredSize(new Dimension(100,20));
        schedulerPanel.add(forField);
        
        secondsLabel.setEnabled(false);
        schedulerPanel.add(secondsLabel);
        
        disableSchedulerComponents(startDelayLbl, startDelayField,
        		repeatLbl,repeatField,forLabel,forField,secondsLabel); 
        
        operationPanel.add(schedulerPanel);
	}

	// disable scheduler components
	private void disableSchedulerComponents(JLabel startDelayLbl, JTextField startDelayField, JLabel repeatLabel, 
			JTextField repeatField, JLabel forLabel, JTextField forField, JLabel seconds) {
		startDelayLbl.setEnabled(false);
		startDelayField.setEnabled(false);
		repeatLabel.setEnabled(false);
		repeatField.setEnabled(false);
		forLabel.setEnabled(false);
		forField.setEnabled(false);
		seconds.setEnabled(false);
	}
	
	// enable scheduler components
	private void enableSchedulerComponents(JLabel startDelayLbl, JTextField startDelayField, JLabel repeatLabel, 
			JTextField repeatField, JLabel forLabel, JTextField forField, JLabel seconds) {
		startDelayLbl.setEnabled(true);
		startDelayField.setEnabled(true);
		repeatLabel.setEnabled(true);
		repeatField.setEnabled(true);
		forLabel.setEnabled(true);
		forField.setEnabled(true);
		seconds.setEnabled(true);
	}
	
	// thread started
	public void threadStarted(JButton action, JButton cancel) {
		action.setEnabled(false);
		cancel.setEnabled(true);
	}
	
	// heap thread done
	public void heapThreadDone() {
		heapBtn.setEnabled(true);	
		heapCancelBtn.setEnabled(false);
	}
	
	// thread dump thread done
	public void threadThreadDone() {
		threadBtn.setEnabled(true);
		threadCancelBtn.setEnabled(false);
	}
	
	// scheduler enabled/disabled
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		
		if (source == hScheduleChkBox) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				enableSchedulerComponents(heapStartDelayLbl,heapStartDelayField,
		        		heapRepeatLbl,heapRepeatField,heapForLabel,heapForField,secondsLabel);
			} else {
				disableSchedulerComponents(heapStartDelayLbl,heapStartDelayField,
		        		heapRepeatLbl,heapRepeatField,heapForLabel,heapForField,secondsLabel);
			}
		} else if (source == tScheduleChkBox) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				enableSchedulerComponents(threadStartDelayLbl,threadStartDelayField,
						threadRepeatLabel, threadRepeatField, threadForLabel, threadForField,secondsLabel2);
			} else {
				disableSchedulerComponents(threadStartDelayLbl, threadStartDelayField,
						threadRepeatLabel, threadRepeatField, threadForLabel, threadForField,secondsLabel2);
			}
		}
	}	
}
