package com.alpercakan.cmpe434;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class CellData {
    int x, y, color, heading;
    int dists[] = new int[4];

    public CellData(int x, int y, int color, int heading, int dists[]) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.heading = heading;
        this.dists = Arrays.copyOf(dists, 4);;
    }
}

public class FinalProjectPC extends JFrame {

    public static void drawArrow(final Graphics2D gfx,
                                 final Point2D start,
                                 final Point2D end,
                                 final float arrowSize,
                                 final boolean isCellBlack) {
        final double startx = start.getX();
        final double starty = start.getY();

        if (isCellBlack)
        	gfx.setPaint(Color.WHITE);
        else
        	gfx.setPaint(Color.BLACK);
        
        gfx.setStroke( new BasicStroke( 3.0f ));
        final double deltax = startx - end.getX();
        final double result;
        if (deltax == 0.0d) {
            result = Math.PI / 2;
        }
        else {
            result = Math.atan((starty - end.getY()) / deltax) + (startx < end.getX() ? Math.PI : 0);
        }

        final double angle = result;

        final double arrowAngle = Math.PI / 12.0d;

        final double x1 = arrowSize * Math.cos(angle - arrowAngle);
        final double y1 = arrowSize * Math.sin(angle - arrowAngle);
        final double x2 = arrowSize * Math.cos(angle + arrowAngle);
        final double y2 = arrowSize * Math.sin(angle + arrowAngle);

        final double cx = (arrowSize / 2.0f) * Math.cos(angle);
        final double cy = (arrowSize / 2.0f) * Math.sin(angle);

        final GeneralPath polygon = new GeneralPath();
        polygon.moveTo(end.getX(), end.getY());
        polygon.lineTo(end.getX() + x1, end.getY() + y1);
        polygon.lineTo(end.getX() + x2, end.getY() + y2);
        polygon.closePath();
        gfx.fill(polygon);

        gfx.drawLine((int) startx, (int) starty, (int) (end.getX() + cx), (int) (end.getY() + cy));
    }

    static InputStream inputStream;
    static DataInputStream dataInputStream;

    public static final int BORDER_THICKNESS = 5,
                            CELL_WIDTH = 50,
                            CELL_HEIGHT = 50,
                            X_MARGIN = 50,
                            Y_MARGIN = 50;

    public FinalProjectPC() {
        super("Map Monitor for CmpE 434");
        setSize(800, 800);
        setVisible(true);
    }


    CellData cells[][] = new CellData[20][20];
    int properDists[][][] = new int[20][20][4];
    boolean properDistInitted[][] = new boolean[20][20];
    int currentX = -1, currentY = -1;
    
    public static int[] shift(int []arr, int shiftAmount) {
    	int []ret = new int[arr.length];
    	
    	for (int i = 0; i < arr.length; ++i) {
    		ret[(i + arr.length + shiftAmount) % arr.length] = arr[i];
    	}
    	
    	return ret;
    }
    
    public void paint(Graphics g) {
        super.paint(g);

        for (int x = 0; x < 20; ++x) {
        	for (int y = 0; y < 20; ++y) {
        		if (cells[x][y] == null)
        			continue;
        		
        		int color = cells[x][y].color;
                int startX = BORDER_THICKNESS * (x + 1) + CELL_WIDTH * x;
                int startY = BORDER_THICKNESS * (13 - y) + CELL_HEIGHT * (12 - y);

                Graphics2D graphics2D = (Graphics2D) g;

                graphics2D.setPaint(getColor(color));
                startX += X_MARGIN;
                startY += Y_MARGIN;
                graphics2D.fillRect(startX, startY, CELL_WIDTH, CELL_HEIGHT);

                
                if (!properDistInitted[x][y]) {
                	properDistInitted[x][y] = true;
                	properDists[x][y] = shift(cells[x][y].dists, cells[x][y].heading);
                    // 1 means wall exists.
                    
                }
                
                graphics2D.setColor(Color.BLACK);
                graphics2D.setStroke(new BasicStroke(3));
                
                for (int i = 0; i < 4; ++i) {
                	if (properDists[x][y][i] == 0)
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

                if (x != currentX || y != currentY)
                    continue;
                
                Point2D.Double start = new Point2D.Double(), end = new Point2D.Double();

                switch (cells[x][y].heading) {
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

                drawArrow(graphics2D, start, end, 15, color == 7);
        	}
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

    public void updateMap(int x, int y, int color, int heading, int dists[]) {
    	cells[x][y] = new CellData(x, y, color, heading, dists);

        repaint();
    }

    public static int lastX, lastY, lastColor, lastHeading;
    public static int lastDists[] = new int[4];

    
    public static void getMapData() throws Exception {
        while (true) {
            int read = 0;

            while (read != -2) {
                read = dataInputStream.readInt();
            }

            lastX = dataInputStream.readInt();
            lastY = dataInputStream.readInt();
            lastColor = dataInputStream.readInt();
            lastHeading = dataInputStream.readInt();
            
            for (int i = 0; i < 4; ++i)
            	lastDists[i] = dataInputStream.readInt();

            if (!((lastX < 0) || (lastY < 0) || (lastColor < 0) || (lastHeading < 0)))
                return;
        }
    }


    public static Scanner sc = new Scanner(System.in);

    /*public static void getMapData() {
        lastX = sc.nextInt();
        lastY = sc.nextInt();
        lastColor = sc.nextInt();
        lastHeading = sc.nextInt();
    }*/

    public static void main(String []args) throws Exception {
        FinalProjectPC monitor = new FinalProjectPC();

        monitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        monitor.setAlwaysOnTop(true);

        String ip = "10.0.1.1";

        @SuppressWarnings("resource")
        Socket socket = new Socket(ip, 1234);
        System.out.println("Connected!");

        inputStream = socket.getInputStream();
        dataInputStream = new DataInputStream(inputStream);

        while(true) {
            getMapData();
            monitor.currentX = lastX;
            monitor.currentY = lastY;
            monitor.updateMap(lastX, lastY, lastColor, lastHeading, lastDists);
        }
    }
}
