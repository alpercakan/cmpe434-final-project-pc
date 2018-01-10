package com.alpercakan.cmpe434;

import javax.swing.*;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class IntPoint {
	int x, y;
	
	public IntPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {
		return "(" + x + ", " + y + ")";
	}
}

class CellData {
    int x, y, color;
    int dists[] = new int[4];

    public CellData(int x, int y, int color, int dists[]) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.dists = Arrays.copyOf(dists, 4);;
    }
}

public class PCGui extends JFrame {

    static final int BT_PORT_NUMBER = 1234;
    static final int BT_PACKET_START = -2, BT_PACKET_END = -3;
    static final int BT_DATA_HEADING = -13,
    				 BT_DATA_XY = -14,
    				 BT_DATA_MAP_INFO = -15,
    				 BT_DATA_MODE = -16,
    				 BT_DATA_POSSIBLE = -17,
    				 BT_DATA_DEBUG = -18,
    				 BT_DATA_READ_MAP = -19,
    				 BT_DATA_INTEG_MAP = -50;
    static final int BT_MAPPING_MODE = -8, BT_EXEC_MODE = -9;

    static InputStream inputStream;
    static DataInputStream dataInputStream;

    public static final int BORDER_THICKNESS = 5,
                            CELL_WIDTH = 50,
                            CELL_HEIGHT = 50,
                            X_MARGIN = 50,
                            Y_MARGIN = 50;

    public PCGui() {
        super("Map Monitor for CmpE 434");
        setSize(800, 800);
        setVisible(true);
    }


    CellData cells[][] = new CellData[20][20],
    		 readMap[][] = new CellData[20][20];
    int currentX = 6, currentY = 6, currentHeading = 0;
    ArrayList<IntPoint> possibleList = new ArrayList<>();
    boolean localizedAlready = false;
    
    /*
    public static int[] shift(int []arr, int shiftAmount) {
    	int []ret = new int[arr.length];
    	
    	for (int i = 0; i < arr.length; ++i) {
    		ret[(i + arr.length + shiftAmount) % arr.length] = arr[i];
    	}
    	
    	return ret;
    }*/
    
    public void drawMapping(Graphics g) {
    	Graphics2D graphics2D = (Graphics2D) g;
    	
    	for (int x = 0; x < 20; ++x) {
        	for (int y = 0; y < 20; ++y) {
        		if (cells[x][y] == null)
        			continue;
        		
        		int color = cells[x][y].color;
                int startX = BORDER_THICKNESS * (x + 1) + CELL_WIDTH * x;
                int startY = BORDER_THICKNESS * (13 - y) + CELL_HEIGHT * (12 - y);

                graphics2D.setPaint(getColor(color));
                startX += X_MARGIN;
                startY += Y_MARGIN;
                graphics2D.fillRect(startX, startY, CELL_WIDTH, CELL_HEIGHT);
                
                graphics2D.setColor(Color.BLACK);
                graphics2D.setStroke(new BasicStroke(3));
                
                for (int i = 0; i < 4; ++i) {
                	if (cells[x][y].dists[i] == 0)
                		continue;
                	
                	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                	
                	switch (i) {
                	case 0: // NORTH WALL
                		x1 = startX;
                		x2 = startX + CELL_WIDTH;
                		y1 = y2 = startY;
                		break;
                		
                	case 1: // EAST WALL
                		x1 = x2 = startX + CELL_WIDTH;
                		y1 = startY;
                		y2 = y1 + CELL_HEIGHT;
                		break;
                		
                	case 2: // SOUTH WALL
                		x1 = startX;
                		x2 = startX + CELL_WIDTH;
                		y1 = y2 = startY + CELL_HEIGHT;
                		break;
                		
                	case 3: // WEST WALL
                		x1 = x2 = startX;
                		y1 = startY;
                		y2 = y1 + CELL_HEIGHT;
                	}
                	
                	graphics2D.drawLine(x1, y1, x2, y2);	
                }
        	}
        }
    	
        Point2D.Double start = new Point2D.Double(), end = new Point2D.Double();
        int startX = BORDER_THICKNESS * (currentX + 1) + CELL_WIDTH * currentX;
        int startY = BORDER_THICKNESS * (13 - currentY) + CELL_HEIGHT * (12 - currentY);

        switch (currentHeading) {
            case 0:
                start.x = startX + CELL_WIDTH / 2;
                start.y = startY + CELL_HEIGHT;
                end.x = start.x;
                end.y = startY;
                break;
            case 1:
                start.x = startX;
                start.y = startY + CELL_HEIGHT / 2;
                end.x = startX + CELL_WIDTH;
                end.y = startY + CELL_HEIGHT / 2;
                break;
            case 2:
                start.x = startX + CELL_WIDTH / 2;
                start.y = startY;
                end.x = start.x - .001;
                end.y = start.y + CELL_HEIGHT;
                break;
            case 3:
                start.x = startX + CELL_WIDTH;
                start.y = startY + CELL_HEIGHT / 2;
                end.x = startX;
                end.y = start.y;
                break;
        }

        //Utils.drawArrow(graphics2D, start, end, 15, cells[currentX][currentY].color == 7);
        graphics2D.setPaint(Color.BLACK);
		startX = BORDER_THICKNESS * (currentX + 1) + CELL_WIDTH * currentX;
        startY = BORDER_THICKNESS * (13 - currentY) + CELL_HEIGHT * (12 - currentY);
     
        startX += X_MARGIN + CELL_WIDTH / 2 - 5;
        startY += Y_MARGIN + CELL_HEIGHT / 2 - 5;

		graphics2D.fillOval(startX, startY, 10, 10);
    }
    
    public void drawExec(Graphics g) {
    	Graphics2D graphics2D = (Graphics2D) g;
    	
    	for (int x = 0; x < 20; ++x) {
        	for (int y = 0; y < 20; ++y) {
        		if (readMap[x][y] == null)
        			continue;
        		
        		int color = readMap[x][y].color;
                int startX = BORDER_THICKNESS * (x + 1) + CELL_WIDTH * x;
                int startY = BORDER_THICKNESS * (13 - y) + CELL_HEIGHT * (12 - y);

                graphics2D.setPaint(getColor(color));
                startX += X_MARGIN;
                startY += Y_MARGIN;
                graphics2D.fillRect(startX, startY, CELL_WIDTH, CELL_HEIGHT);
                
                graphics2D.setColor(Color.BLACK);
                graphics2D.setStroke(new BasicStroke(3));
                
                for (int i = 0; i < 4; ++i) {
                	if (readMap[x][y].dists[i] == 0)
                		continue;
                	
                	int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                	
                	switch (i) {
                	case 0: // NORTH WALL
                		x1 = startX;
                		x2 = startX + CELL_WIDTH;
                		y1 = y2 = startY;
                		break;
                		
                	case 1: // EAST WALL
                		x1 = x2 = startX + CELL_WIDTH;
                		y1 = startY;
                		y2 = y1 + CELL_HEIGHT;
                		break;
                		
                	case 2: // SOUTH WALL
                		x1 = startX;
                		x2 = startX + CELL_WIDTH;
                		y1 = y2 = startY + CELL_HEIGHT;
                		break;
                		
                	case 3: // WEST WALL
                		x1 = x2 = startX;
                		y1 = startY;
                		y2 = y1 + CELL_HEIGHT;
                	}
                	
                	graphics2D.drawLine(x1, y1, x2, y2);	
                }
        	}
        }
    	
    	if (localizedAlready) {
    		Point2D.Double start = new Point2D.Double(), end = new Point2D.Double();
            int startX = BORDER_THICKNESS * (currentX + 1) + CELL_WIDTH * currentX;
            int startY = BORDER_THICKNESS * (13 - currentY) + CELL_HEIGHT * (12 - currentY);

            switch (currentHeading) {
                case 0:
                    start.x = startX + CELL_WIDTH / 2;
                    start.y = startY + CELL_HEIGHT;
                    end.x = start.x;
                    end.y = startY;
                    break;
                case 1:
                    start.x = startX;
                    start.y = startY + CELL_HEIGHT / 2;
                    end.x = startX + CELL_WIDTH;
                    end.y = startY + CELL_HEIGHT / 2;
                    break;
                case 2:
                    start.x = startX + CELL_WIDTH / 2;
                    start.y = startY;
                    end.x = start.x - .001;
                    end.y = start.y + CELL_HEIGHT;
                    break;
                case 3:
                    start.x = startX + CELL_WIDTH;
                    start.y = startY + CELL_HEIGHT / 2;
                    end.x = startX;
                    end.y = start.y;
                    break;
            }

            /*System.out.println("Drawing arrow to " + currentX + " " + currentY);
            Utils.drawArrow(graphics2D, start, end, 15, readMap[currentX][currentY].color == 7);*/
            graphics2D.setPaint(Color.BLACK);
			startX = BORDER_THICKNESS * (currentX + 1) + CELL_WIDTH * currentX;
            startY = BORDER_THICKNESS * (13 - currentY) + CELL_HEIGHT * (12 - currentY);
         
            startX += X_MARGIN + CELL_WIDTH / 2 - 5;
            startY += Y_MARGIN + CELL_HEIGHT / 2 - 5;

			graphics2D.fillOval(startX, startY, 10, 10);
    	} else {
    		for (int i = 0; i < possibleList.size(); ++i) {
    			graphics2D.setPaint(Color.MAGENTA);
    			int startX = BORDER_THICKNESS * (possibleList.get(i).x + 1) + CELL_WIDTH * possibleList.get(i).x;
                int startY = BORDER_THICKNESS * (13 - possibleList.get(i).y) + CELL_HEIGHT * (12 - possibleList.get(i).y);
             
                startX += X_MARGIN + CELL_WIDTH / 2 - 5;
                startY += Y_MARGIN + CELL_HEIGHT / 2 - 5;

    			graphics2D.fillOval(startX, startY, 10, 10);
    		}
    	}  	
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        
        if (mode == null)
        	return;

        try {
        	if (PCGui.mode == TaskMode.EXECUTION) {
        		drawExec(g);
        	} else {
        		drawMapping(g);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public static Color []colors = {
            null, // unknown
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.MAGENTA,
            Color.YELLOW,
            Color.ORANGE,
            Color.WHITE,
            Color.BLACK,
            Color.PINK,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.DARK_GRAY,
            Color.CYAN,
            null // brown
    };

    public static Color getColor(int color) {
        return colors[color + 1];
    }

    /**
     * @return Packet type
     * @throws IOException 
     */
    public static int packetStart() throws IOException {
        int read = 0;

        while (read != BT_PACKET_START) {
            read = dataInputStream.readInt();
        }

        int ret = dataInputStream.readInt();
        System.out.println("Packet type: " + ret);
        return ret;
    }

    public static void packetEnd() throws IOException {
        int read = 0;

        while (read != BT_PACKET_END) {
            read = dataInputStream.readInt();
        }
    }

    enum TaskMode { MAPPING, EXECUTION, ERROR };

    public static TaskMode getTaskMode() throws IOException {
        int read = packetStart();

        if (read != BT_DATA_MODE) {
            packetEnd();
            return TaskMode.ERROR;
        }

        TaskMode ret;

        read = dataInputStream.readInt();

        switch (read) {
            case BT_MAPPING_MODE:
                ret = TaskMode.MAPPING;
                break;
            case BT_EXEC_MODE:
                ret = TaskMode.EXECUTION;
                break;
            default:
                ret = TaskMode.ERROR;
                break;
        }

        packetEnd();
        System.out.println(ret.name());
        return ret;
    }
    
    static void handleReadMap() throws IOException {
    	int size = dataInputStream.readInt();
    	
    	for (int i = 0; i < size; ++i) {
    		/*
    		 * 	dataOutputStream.writeInt(c.x);
				dataOutputStream.writeInt(c.y);
				
				for (int j = 0; j < 4; ++j) {
					dataOutputStream.writeFloat(c.dists[j]);
				}
				
				dataOutputStream.writeInt(c.color);
    		 */
    		int x, y, color;
    		x = dataInputStream.readInt();
    		y = dataInputStream.readInt();
    		
    		color = dataInputStream.readInt();
    		
    		int dists[] = new int[4];
    		
    		for (int j = 0; j < 4; ++j) {
    			dists[j] = dataInputStream.readInt();
    		}
    		
    		monitor.readMap[x][y] = new CellData(x, y, color, dists);
    	}
    }
    
    static void handleIntegMap() throws IOException {
    	for (int x = 0; x < 20; ++x) {
    		for (int y = 0; y < 20; ++y) {
        		monitor.readMap[x][y] = null;
        	}	
    	}
    	
    	int size = dataInputStream.readInt();
    	
    	while (size-- > 0) {
    		/*
    		 * 	dataOutputStream.writeInt(c.x);
				dataOutputStream.writeInt(c.y);
				
				for (int j = 0; j < 4; ++j) {
					dataOutputStream.writeFloat(c.dists[j]);
				}
				
				dataOutputStream.writeInt(c.color);
    		 */
    		int x, y, color;
    		x = dataInputStream.readInt();
    		y = dataInputStream.readInt();
    		
    		color = dataInputStream.readInt();
    		
    		int dists[] = new int[4];
    		
    		for (int j = 0; j < 4; ++j) {
    			dists[j] = dataInputStream.readInt();
    		}
    		
    		monitor.readMap[x][y] = new CellData(x, y, color, dists);
    	}
    	
    	monitor.localizedAlready = true;
    }
    
    static void handleMapInfo() throws IOException {
    	if (mode == TaskMode.EXECUTION && monitor.localizedAlready) {
    		if (monitor.readMap[monitor.currentX][monitor.currentY] == null) {
        		int fakeArr[] = { 0, 0, 0, 0 };
        		monitor.readMap[monitor.currentX][monitor.currentY] = new CellData(monitor.currentX, monitor.currentY, 6, fakeArr);
        	}
        	
        	monitor.currentX = dataInputStream.readInt();
        	monitor.currentY = dataInputStream.readInt();
        	System.out.println(monitor.currentX + " " + monitor.currentY);
            monitor.readMap[monitor.currentX][monitor.currentY].color = dataInputStream.readInt();
            // TODO delete
            System.out.println(colors[monitor.readMap[monitor.currentX][monitor.currentY].color + 1].toString());
            monitor.currentHeading = dataInputStream.readInt();

            for (int i = 0; i < 4; ++i)
            	monitor.readMap[monitor.currentX][monitor.currentY].dists[i] = dataInputStream.readInt();    		
    	} else {
    		if (monitor.cells[monitor.currentX][monitor.currentY] == null) {
        		int fakeArr[] = { 0, 0, 0, 0 };
        		monitor.cells[monitor.currentX][monitor.currentY] = new CellData(monitor.currentX, monitor.currentY, 6, fakeArr);
        	}
        	
        	monitor.currentX = dataInputStream.readInt();
        	monitor.currentY = dataInputStream.readInt();
            monitor.cells[monitor.currentX][monitor.currentY].color = dataInputStream.readInt();
            monitor.currentHeading = dataInputStream.readInt();

            for (int i = 0; i < 4; ++i)
            	monitor.cells[monitor.currentX][monitor.currentY].dists[i] = dataInputStream.readInt();	
    	}    	
    }
    
    static void handleHeading() throws IOException {
    	monitor.currentHeading = dataInputStream.readInt();
    }
    
    static void handleDebug() throws IOException {
    	int size = dataInputStream.readInt();
    	
    	char arr[] = new char[size];
    	
    	for (int i = 0; i < size; ++i) {
    		arr[i] = dataInputStream.readChar();
    	}
    	
    	System.out.println("DEBUG: "+ String.valueOf(arr));
    }
    
    static void handleXY() throws IOException {
    	monitor.currentX = dataInputStream.readInt();
        monitor.currentY = dataInputStream.readInt();
        
        System.out.println("current = " + monitor.currentX + " " + monitor.currentY);
    }

    static void handlePossible() throws IOException {
    	int size = dataInputStream.readInt();
    	monitor.possibleList = new ArrayList<>();
    	
    	while (size-- != 0) {
    		int x, y, heading;
    		x = dataInputStream.readInt();
    		y = dataInputStream.readInt();
			heading = dataInputStream.readInt();
			monitor.possibleList.add(new IntPoint(x, y));
    	}
    }
    
    public static PCGui monitor;
    public static TaskMode mode;
    
    public static void main(String []args) throws Exception {
        monitor = new PCGui();

        monitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        monitor.setAlwaysOnTop(true);
        
        while (true) {
        	try {
            	String ip = "10.0.1.1";

                @SuppressWarnings("resource")
                Socket socket = new Socket(ip, BT_PORT_NUMBER);
                System.out.println("Connected!");

                inputStream = socket.getInputStream();
                dataInputStream = new DataInputStream(inputStream);

                mode = getTaskMode();

                while(true) {
                    int type = packetStart();

                    switch (type) {
                    	case BT_DATA_READ_MAP:
                    		handleReadMap();
                    		break;
                        case BT_DATA_XY:
                        	handleXY();
                            break;
                        case BT_DATA_MAP_INFO:
                            handleMapInfo();
                            break;
                        case BT_DATA_HEADING:
                            handleHeading();
                            break;
                        case BT_DATA_POSSIBLE:
                        	handlePossible();
                        	break;
                        case BT_DATA_DEBUG:
                        	handleDebug();
                        	break;
                        	
                        case BT_DATA_INTEG_MAP:
                        	handleIntegMap();
                        	break;
                    }
                    
                    monitor.repaint();

                    packetEnd();
                }	
            } catch (Exception e) {
            	mode = null;
            	monitor.cells = new CellData[20][20];
            	monitor.readMap = new CellData[20][20];
            	monitor.currentX = monitor.currentY = 6;
            	monitor.currentHeading = 0;
            	monitor.possibleList = new ArrayList<>();
            	monitor.localizedAlready = false;
            	dataInputStream = null;
            	inputStream = null;
            }	
        }
    }
}
