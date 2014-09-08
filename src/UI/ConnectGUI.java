package UI;
import logic.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.management.MBeanServerConnection;
import javax.swing.*;  

@SuppressWarnings("serial")
public class ConnectGUI extends JFrame{
	
	final JFrame frame = this;
	/**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
	 * @throws IOException 
     */
    private ConnectGUI() {
        //Create and set up the window.
        super("MBean Toolkit: New Connection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Container contentPane = getContentPane();
        //contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        
        // main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0,1));
        
        // top picture panel
        JPanel picPanel = new JPanel();
        try {
        	BufferedImage connectionImg = ImageIO.read(new File("images/newConnection.PNG"));
        	JLabel picLabel = new JLabel(new ImageIcon(connectionImg));
            picLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            picPanel.add(picLabel);
        } catch (IOException e) {
        	JOptionPane.showMessageDialog(frame, e, "Error connecting to server", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
        }
        picPanel.setLayout(new BoxLayout(picPanel, BoxLayout.Y_AXIS));        
        mainPanel.add(picPanel);
        
        // middle connection panel
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

        JLabel processLbl = new JLabel("Remote Process:");
        processLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        middlePanel.add(processLbl);
        
        final JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(400,20));
        middlePanel.add(field);
        
        JLabel usageLbl = new JLabel("Usage: <hostname>:<port>");
        usageLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        usageLbl.setForeground(Color.gray);
        Font usageFont = usageLbl.getFont();
        usageLbl.setFont(new Font(usageFont.getName(), Font.ITALIC, usageFont.getSize()));
        middlePanel.add(usageLbl);
        mainPanel.add(middlePanel);
        
        // bottom connect button panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        mainPanel.add(bottomPanel);
        
        final JButton connectBtn = new JButton("Connect");
        bottomPanel.add(connectBtn);
        
        JButton cancelBtn = new JButton("Cancel");
        bottomPanel.add(cancelBtn);
        
        add(mainPanel);
        
        //Display the window.
        pack();
        setMinimumSize(new Dimension(400,300));
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setBounds(screenSize.width/2 - frameSize.width/2 , screenSize.height/2 - frameSize.height/2, 400, 300);
        setVisible(true);
        
        // create connection
        connectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					final MBeanServerConnection mBeanConnection = JmxConnect.connectToServer(field.getText());			
					javax.swing.SwingUtilities.invokeLater(new Runnable()  {
			            public void run()  {
			            	new ToolGui(mBeanConnection,frame,field.getText());
			            }
			        });	
					frame.dispose();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Error connecting to server", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
		});
        
        field.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
				try {
					final MBeanServerConnection mBeanConnection = JmxConnect.connectToServer(field.getText());			
					javax.swing.SwingUtilities.invokeLater(new Runnable()  {
			            public void run()  {
			            	new ToolGui(mBeanConnection,frame,field.getText());
			            }
			        });	
					frame.dispose();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(frame, e.getMessage(), "Error connecting to server", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
				}
			}
        });
        
        // close application
        cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
    }
    
    /**
     * Start main connect screen
     */
	public static void main(String[] args)  {
		 //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()  {
            public void run()  {
            	new ConnectGUI();
            	//new ToolGui();
            }
        });
	}

}
