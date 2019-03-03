import java.awt.*;
import java.util.Random;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TSpinTutor extends JFrame {
	//global random generator
	static Random r = new Random();
	//graphics constants
	static int sw = 1920;
	static int sh = 1080;
	static int bSize = 36;
	static int bSizeHalf = 18;
	static int bSizePrev = 28;
	static int bSizePrevFuture = 22;
	static int gridLeft = 308;
	static int gridRight = 632;
	static int gridTop = 160;
	static int gridBot = 844;
	static int numRows = 20;
	static int numCols = 10;
	//timing vars
	static long time;
    static int fps = 60;
    //tetris vars
    static enum Tetrimino {
        I, O, T, S, Z, J, L
    }
    static Tetrimino curBlock = null;
    static Tetrimino nextBlock = null;
    static boolean boardState[][] = new boolean[numRows][numCols];
    static int shadowCols[] = new int[4];
    static int shadowRows[] = new int[4];
    static int curShadowCol;
    //ui vars
    static Font uiFont = new Font("Courier", Font.BOLD,36);
    static long frameTime = 0;
	
	TSpinTutor() {
		//set the frame properties for a transparent, always on top overlay
        setUndecorated(true);
        setSize(sw, sh);
        setLocation(0, 0);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setBackground(new Color(0,0,0,0));
	}
	
	public static class Panel extends JPanel {
        public Panel() {
            setBackground(new Color(0,0,0,0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            //text indicators
            g.setFont(uiFont);
            g.drawString(String.format("Current Tetrimino: %s",curBlock), 50, 40);
            g.drawString(String.format("Next Tetrimino: %s",nextBlock), 50, 65);
            g.drawString(String.format("Prev Frame Time: %dms",frameTime), 50, 90);
            //board indicator
            g.setColor(Color.RED);
            for (int i = 0; i < numRows; ++i) {
            	for (int r = 0; r < numCols; ++r) {
            		if (boardState[i][r]) {
            			g.fillRect(gridRight + 2*bSize + bSize*r,gridTop + bSize*i,bSize,bSize);	
            		}
            	}
            }
        }
    }
	
	public static void main(String[] args) throws InterruptedException, AWTException {
        final TSpinTutor tutor = new TSpinTutor();   
        Panel panel = new Panel();
        tutor.add(panel);
        tutor.setVisible(true);
        Rectangle screenRect;
        BufferedImage capture;
        while(true) {
        	time = System.nanoTime();
        	screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        	capture = new Robot().createScreenCapture(screenRect);
			//check pixel 760,223 color to determine next tetrimino
        	switch (capture.getRGB(760,223)) {
			case -15658735: //I block
				nextBlock = Tetrimino.I;
				break;
			case -4623607: //O block
				nextBlock = Tetrimino.O;
				break;
			case -8051578: //T block
				nextBlock = Tetrimino.T;
				break;
			case -11226340: //S block
				nextBlock = Tetrimino.S;
				break;
			case -2418657: //Z block
				nextBlock = Tetrimino.Z;
				break;
			case -16757074: //J block
				nextBlock = Tetrimino.J;
				break;
			case -41472: //L block
				nextBlock = Tetrimino.L;
				break;
			default: //no block color detected
				break;
			}
        	//check the top-left corner of each grid space in search of shadow indicator to determine current tetrimino
        	shadowCols[0] = shadowCols[1] = shadowCols[2] = shadowCols[3] = -1;
			curShadowCol = 0;
        	for (int x = gridLeft; x <= gridRight; x+= bSize) {
        		for (int y = gridTop; y <= gridBot; y += bSize) {
        			switch (capture.getRGB(x,y)) {
        			case -8335379: //I block
        				curBlock = Tetrimino.I;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				
        				break;
        			case -6784: //O block
        				curBlock = Tetrimino.O;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			case -3500340: //T block
        				curBlock = Tetrimino.T;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			case -4923500: //S block
        				curBlock = Tetrimino.S;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			case -617316: //Z block
        				curBlock = Tetrimino.Z;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			case -8342818: //J block
        				curBlock = Tetrimino.J;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			case -17280: //L block
        				curBlock = Tetrimino.L;
        				shadowRows[curShadowCol] = (y-gridTop)/bSize;
        				shadowCols[curShadowCol++] = (x-gridLeft)/bSize;
        				break;
        			default: //no block color detected
        				break;
        			}
        		}
        	}
			//construct the board state by checking for blocks of any color across the grid, ignoring blocks above a shadow indicator block
        	for (int x = gridLeft; x <= gridRight; x+= bSize) {
        		for (int y = gridBot; y >= gridTop; y -= bSize) {
        			int r = (x-gridLeft)/bSize;
        			int i = (y-gridTop)/bSize;
        			//any block above and in the same column as a shadow indicator is guaranteed to be empty
    				if ((r == shadowCols[0] && i < shadowRows[0]) || (r == shadowCols[1] && i < shadowRows[1]) ||
    						(r == shadowCols[2] && i < shadowRows[2]) || (r == shadowCols[3] && i < shadowRows[3])) {
    					boardState[i][r] = false;
        				continue;
        			}
        			switch (capture.getRGB(x+bSizeHalf,y+bSizeHalf)) {
        			case -16738602: //I block
        				boardState[i][r] = true;
        				break;
        			case -18944: //O block
        				boardState[i][r] = true;
        				break;
        			case -8117116: //T block
        				boardState[i][r] = true;
        				break;
        			case -11357672: //S block
        				boardState[i][r] = true;
        				break;
        			case -2223080: //Z block
        				boardState[i][r] = true;
        				break;
        			case -16757331: //J block
        				boardState[i][r] = true;
        				break;
        			case -41728: //L block
        				boardState[i][r] = true;
        				break;
        			case -2171938: //garbage block
        				boardState[i][r] = true;
        				break;
        			default: //no block color detected
        				boardState[i][r] = false;
        				break;
        			}
        		}
        	}
            tutor.repaint();
            frameTime  = (System.nanoTime() - time)/1000000;
            long sleepTime = (1000/fps) - frameTime;
            if (sleepTime > 0) 
            	Thread.sleep(sleepTime);
        }
    }    
}