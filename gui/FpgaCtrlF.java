package prjgui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;

import javax.swing.JTextPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.awt.FlowLayout;
import java.io.*;
import java.util.Locale;

import javax.swing.border.EtchedBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

// import prjgui.SerialNetw;


public class FpgaCtrlF {

	private JFrame FG_frame;
	private JTextField cmd_textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
        SerialNetw.initSerialNetw();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FpgaCtrlF window = new FpgaCtrlF();
					window.FG_frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public FpgaCtrlF() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		FG_frame = new JFrame();
		FG_frame.setTitle("FPGA Control GUI");
		FG_frame.setBounds(100, 100, 812, 547);
		FG_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		FG_frame.getContentPane().setLayout(springLayout);
		
		JPanel Cmd_panel = new JPanel();
		springLayout.putConstraint(SpringLayout.NORTH, Cmd_panel, -45, SpringLayout.SOUTH, FG_frame.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, Cmd_panel, -10, SpringLayout.EAST, FG_frame.getContentPane());
		Cmd_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		FlowLayout flowLayout = (FlowLayout) Cmd_panel.getLayout();
		flowLayout.setHgap(10);
		flowLayout.setAlignment(FlowLayout.LEFT);
		springLayout.putConstraint(SpringLayout.WEST, Cmd_panel, 10, SpringLayout.WEST, FG_frame.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, Cmd_panel, -10, SpringLayout.SOUTH, FG_frame.getContentPane());
		FG_frame.getContentPane().add(Cmd_panel);
		
		JScrollPane scrollPane = new JScrollPane();
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.NORTH, Cmd_panel);
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 250, SpringLayout.WEST, FG_frame.getContentPane());
		
		JLabel lblNewLabel = new JLabel("cmd>> ");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		Cmd_panel.add(lblNewLabel);
		
		cmd_textField = new JTextField();
		cmd_textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CommandHandler(cmd_textField.getText());
				cmd_textField.setText("");
			}
		});
		Cmd_panel.add(cmd_textField);
		cmd_textField.setColumns(60);
		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, FG_frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, FG_frame.getContentPane());
		FG_frame.getContentPane().add(scrollPane);
		
		Gr_panel = new JPanel();
		Gr_panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		springLayout.putConstraint(SpringLayout.NORTH, Gr_panel, 10, SpringLayout.NORTH, FG_frame.getContentPane());
		springLayout.putConstraint(SpringLayout.WEST, Gr_panel, 10, SpringLayout.EAST, scrollPane);
		springLayout.putConstraint(SpringLayout.SOUTH, Gr_panel, -10, SpringLayout.NORTH, Cmd_panel);
		springLayout.putConstraint(SpringLayout.EAST, Gr_panel, -10, SpringLayout.EAST, FG_frame.getContentPane());
		
		tout_textPane = new JTextPane();
		tout_textPane.setEditable(false);
		scrollPane.setViewportView(tout_textPane);
		FG_frame.getContentPane().add(Gr_panel);
		
		JMenuBar menuBar = new JMenuBar();
		FG_frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String dev_name;
				dev_name = SerialNetw.getConName();
				if (dev_name != null) {
					SerialNetw.spDisconn(dev_name);
				}
		        DispUpdate_Timer.stop();
		        System.exit(0);
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
		CreateChart();
        DispUpdate_Timer = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateDynamic();
                DispUpdate_Timer.restart();
            }
        });
        DispUpdate_Timer.start();
        Downl_Cnt = 0;
        Pb_NValues = 0;
        Pb_Ready = false;
	}


	private void CommandHandler(String cmds) {
		String command = cmds, dev_name, fname;
		int  k, q, stat;
		Writer out = null;

		if (command.equals("clc")) {
			tout_textPane.setText("");
		} else if (command.equals("clg")) {
			ClearChart();
		} else if (command.equals("help")) {
			PrintTxtWin("FPGA Control Help:", 1, true);
			PrintTxtWin("    clc - clear text window", 1, true);	
			PrintTxtWin("    clg - clear chart window", 1, true);	
			PrintTxtWin("    dev - list avaliable devices", 1, true);			
			PrintTxtWin("    conn {comX} - connect", 1, true);			
			PrintTxtWin("    disconn - disconnect", 1, true);			
			PrintTxtWin("    .{sendstring}", 1, true);
		} else if (command.equals("dev")) {
			PrintTxtWin("dev...", 0, true);
			for (k = 0; k < SerialNetw.getNDev(); k++) {
				dev_name = SerialNetw.getDevName(k);
				stat = SerialNetw.getDevStat(dev_name);
				if (stat == 2) {
					PrintTxtWin("  " + dev_name + " [connected]", 4, true);									
				} else {
					PrintTxtWin("  " + dev_name + " [avail]", 4, true);																
				}
			}
		} else if (command.startsWith("conn ")) {
			dev_name = command.substring(5);
			if (SerialNetw.getDevStat(dev_name) != 1) {
				PrintTxtWin("*** device unavailable", 3, true);				
			} else {
                (new SerialNetw()).spConnect(dev_name);
				PrintTxtWin("OK", 1, true);				
			}
		} else if (command.equals("disconn")) {
			dev_name = SerialNetw.getConName();
			if (dev_name != null) {
				SerialNetw.spDisconn(dev_name);
				PrintTxtWin("OK", 1, true);				
			} else {
				PrintTxtWin("*** no device connected", 3, true);								
			}
		} else if (command.equals("exit")) {
			dev_name = SerialNetw.getConName();
			if (dev_name != null) {
				SerialNetw.spDisconn(dev_name);
			}
	        DispUpdate_Timer.stop();
	        System.exit(0);
		} else if (command.equals("downl")) {
			if (SerialNetw.getConName() != null) {
				Downl_Cnt = 0;
				Pb_NValues = 0;
		        Pb_Ready = false;
				SerialNetw.SendString("p\n");
				PrintTxtWin(" => starting download:\n ", 1, false);				
			} else {
				PrintTxtWin("*** no connected device", 3, true);				
			}
		} else if (command.startsWith("plot")) {
			if (!Pb_Ready) {
				PrintTxtWin("*** no plot data available", 3, true);
			} else if (Pb_NValues < 2) {
				PrintTxtWin("*** no data points", 3, true);
			} else {
				if (command.equals("plot")) {
					ClearChart();
					for (k = 0; k < Pb_NValues; k++) {
						if (Pb_NValues < NROWS) {
							for (q = 0; q < NTRACES; q++) {
								mtraces[q].addPoint((double)k, Plot_Buffer[k][q]);
							}						
						}
					}					
				} else {
					if (command.length() < 6) {
						PrintTxtWin("*** no file name given", 3, true);
					} else {
						fname = command.substring(5);
						PrintTxtWin(" => attemp to write file \"" + fname + "\"", 1, true);	
						try {
			                out = new OutputStreamWriter(new FileOutputStream("\\Temp\\" + fname));
							for (k = 0; k < Pb_NValues; k++) {
			                    out.write(String.format(Locale.ENGLISH, "%f %f %f %f\n",
			                    		Plot_Buffer[k][0], Plot_Buffer[k][1],
			                    		Plot_Buffer[k][2], Plot_Buffer[k][3]));
							}
							out.close();
						} catch (Exception ex) {
							PrintTxtWin(ex.toString(), 3, true);
						}
			        }

				}
			}
		} else if (command.startsWith(".")) {
			if (SerialNetw.getConName() != null) {
				SerialNetw.SendString(command.substring(1) + "\n");
			} else {
				PrintTxtWin("*** no connected device", 3, true);				
			}
		} else if (command.length() > 0) {
			PrintTxtWin("*** command???: \"" + command + "\"", 3, true);
		}
	}


    private void PrintTxtWin(String twstr, int twstyle, boolean newline) {
        try {
            Document doc = tout_textPane.getStyledDocument();
            StyleConstants.setItalic(TextSet, false);
            StyleConstants.setBold(TextSet, false);
            StyleConstants.setForeground(TextSet, Color.BLACK);
            switch (twstyle) {
                case 0:
                    StyleConstants.setBold(TextSet, true);
                    StyleConstants.setForeground(TextSet, Color.DARK_GRAY);
                    break;
                case 1: StyleConstants.setForeground(TextSet, Color.BLUE);
                    break;
                case 2: StyleConstants.setForeground(TextSet, Color.BLACK);
                    break;
                case 3: StyleConstants.setForeground(TextSet, Color.RED);
                	break;
                case 4: StyleConstants.setForeground(TextSet, Color.GREEN);
                	break;
                default:
                    doc.remove(0, doc.getLength());
            }
            if (twstyle >= 0) {
            	tout_textPane.setCharacterAttributes(TextSet, true);
            	if (newline) {
                    doc.insertString(doc.getLength(), twstr+"\n", TextSet);            		
            	} else {
                    doc.insertString(doc.getLength(), twstr, TextSet);
            	}
            }
        } catch (BadLocationException ex) {
            System.out.println(ex.toString());
        }
    }


	private void ClearChart()
	{
		int  k;
		for (k = 0; k < NTRACES; k++) {
			mtraces[k].removeAllPoints();
			mtraces[k].addPoint(0.0, 0.0);
		}
	}



	private void CreateChart()
	{
		int  k;

		chart = new Chart2D();
		for (k = 0; k < NTRACES; k++) {
			mtraces[k] = new Trace2DSimple();
	        chart.addTrace(mtraces[k]);
		}
        mtraces[0].setColor(Color.blue);     mtraces[0].setName("trace 0");
        mtraces[1].setColor(Color.red);      mtraces[1].setName("trace 1");
        mtraces[2].setColor(Color.green);    mtraces[2].setName("trace 2");
        mtraces[3].setColor(Color.magenta);  mtraces[3].setName("trace 3");
        Gr_panel.setLayout(new BorderLayout(0, 0));
        
        Gr_panel.add(chart);
        Gr_panel.setSize(100,200);
        chart.setVisible(true);
        Gr_panel.setVisible(true);
        Gr_panel.repaint();	
	}



    private void UpdateDynamic() {
    	String  recv_s;
    	String  splits[];
    	int  k;

    	while ((recv_s = SerialNetw.ReadString()) != null) {
			// System.out.println("["+ recv_s + "]");
    		if (recv_s.startsWith("$") || recv_s.startsWith("~")) {
        		PrintTxtWin("+", 1, false);
    			Downl_Cnt++;
    			if (Downl_Cnt >= 25) {
    				Downl_Cnt = 0;
    				PrintTxtWin("\n    ", 1, false);
    			}
    			if (Pb_NValues < NROWS) {
    				splits = recv_s.split(" ");
    				try {
        				for (k = 1; k < splits.length; k++) {
        					Plot_Buffer[Pb_NValues][k-1] = (double)Integer.parseInt(splits[k]);
        				}
        				Pb_NValues++;    					
    				} catch  (NumberFormatException e) {
    			        System.out.println(e.toString());
    			    }
    			}
    			if (recv_s.startsWith("~")) {
        			SerialNetw.SendString("p\n");				
    			}
    		} else if (recv_s.startsWith("###")) {
        		PrintTxtWin("\nDownload finished.", 1, true);
        		Pb_Ready = true;
    		} else {
        		PrintTxtWin(recv_s, 0, true);    			
    		}
    	}
    }


    static int Downl_Cnt;
    static boolean Pb_Ready;
    static final int NROWS = 2048, NCOLS = 4;
    static double Plot_Buffer[][] = new double[NROWS][NCOLS];
    static int Pb_NValues;
    static Chart2D chart;
    static final int NTRACES = 4;
    static ITrace2D mtraces[] = new ITrace2D[4];
    // static ITrace2D trace_1 = new Trace2DLtd(20);
    private SimpleAttributeSet TextSet = new SimpleAttributeSet();
    private Timer DispUpdate_Timer;
    private JPanel Gr_panel;
    private JTextPane tout_textPane;
}