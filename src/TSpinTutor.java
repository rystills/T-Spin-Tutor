import java.awt.*;
import java.util.Random;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * the TSpinTutor class contains a number of static state properties, while serving as the root level frame for our GUI
 *
 */
public class TSpinTutor extends JFrame {
	private static final long serialVersionUID = 1L;
	//global random generator
	static Random r = new Random();
	//graphics constants
	static enum Tetrimino {
        I, O, T, S, Z, J, L
    }
	//TODO: at the moment, the resolution is hard-coded to 1920x1080 as this is the only way to guarantee matching pixel colors. Fuzzy comparison should eventually resolve this.
	static final int sw = 1920;
	static final int sh = 1080;
	static final int bSize = 36;
	static final int bSizeHalf = 18;
	static final int bSizePrev = 28;
	static final int bSizePrevFuture = 22;
	static final int gridLeft = 308;
	static final int gridRight = 632;
	static final int gridTop = 160;
	static final int gridBot = 844;
	static final int numRows = 20;
	static final int numCols = 10;
	//timing
	static long time;
    static final int fps = 60;    
    static long frameTime = 0;
    //ui
    static Font uiFont = new Font("Courier", Font.BOLD,36);
    //capture state
    static Rectangle screenRect;
    static BufferedImage capture;
	//game state
    static Tetrimino curBlock = null;
    static Tetrimino nextBlock = null;
    static boolean boardState[][] = new boolean[numRows][numCols];
    static int shadowCols[] = new int[4];
    static int shadowRows[] = new int[4];
    static int curShadowCol;
    
    /**
     * Apply the necessary properties for a fully transparent, always on top overlay
     */
	TSpinTutor() {
        setUndecorated(true);
        setSize(sw, sh);
        setLocation(0, 0);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setBackground(new Color(0,0,0,0));
	}
	
	/**
	 * The Panel class simply assists in rendering the window
	 *
	 */
	public static class Panel extends JPanel {
		private static final long serialVersionUID = 1L;

		/**
		 * set the background to a fully transparent color to match the parent Frame
		 */
		public Panel() {
            setBackground(new Color(0,0,0,0));
        }

        
        /**
         * re-render the window
         * @param g (Graphics): the graphics instance provided for rendering to the window
         */
		@Override
		protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            //text indicators
            g.setFont(uiFont);
            g.drawString(String.format("Current Tetrimino: %s",curBlock), 50, 40);
            g.drawString(String.format("Next Tetrimino: %s",nextBlock), 50, 65);
            g.drawString(String.format("Prev Frame Time: %dms",frameTime), 50, 90);
            //board indicator
           drawBoard(g);
        }
        
        /**
         * draw the current board state, as determined from the last screenshot analysis. Useful for debugging board state detection.
         * @param g (Graphics): the graphics instance provided in this Panel's paintComponent 
         */
        private void drawBoard(Graphics g) {
        	 g.setColor(Color.RED);
             for (int i = 0; i < numRows; ++i) {
             	for (int r = 0; r < numCols; ++r) {
             		if (boardState[i][r]) {
             			//draw the board state one block size to the right of the actual board
             			g.fillRect(gridRight + 2*bSize + bSize*r,gridTop + bSize*i,bSize,bSize);	
             		}
             	}
             }
        }
    }
	
	/**
	 * check the color of pixel 760,223 to determine the next tetrimino
	 */
	public static void determineNextTetrimino() {
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
	}
	
	/**
	 * check the top-left corner of each grid space for a shadow indicator to determine current tetrimino, and update the shadow position arrays
	 */
	public static void determineCurrentTetrimino() {
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
	}
	
	/**
	 * check the center color of each grid space to determine which spaces contain blocks, ignoring blocks above a shadow indicator block
	 */
	public static void determineBoardState() {
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
	}
	
	public static void main(String[] args) throws InterruptedException, AWTException {
        final TSpinTutor tutor = new TSpinTutor();   
        Panel panel = new Panel();
        tutor.add(panel);
        tutor.setVisible(true);
        while(true) {
        	time = System.nanoTime();
        	//recalculate the screen rectangle in case resolution changes
        	screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        	capture = new Robot().createScreenCapture(screenRect);
			
        	//extract the game state from the screen
        	determineNextTetrimino();
        	determineCurrentTetrimino();
			determineBoardState();
			
			//re-render and wait for the next frame
            tutor.repaint();
            frameTime  = (System.nanoTime() - time)/1000000;
            long sleepTime = (1000/fps) - frameTime;
            if (sleepTime > 0) 
            	Thread.sleep(sleepTime);
        }
    }    
}