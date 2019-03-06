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
        I(0,0,1,0,2,0,3,0), 
        O(0,0,1,0,0,1,1,1), 
        T(0,1,1,0,1,1,1,2),  
        S(0,0,1,0,1,1,2,1),
        Z(0,1,1,1,1,0,2,0),
        J(0,0,0,1,0,2,1,2), 
        L(1,0,1,1,1,2,0,2);
		private int[][] pos;
		Tetrimino(int x0,int y0, int x1,int y1, int x2,int y2, int x3,int y3) {
			int maxX = Math.max(Math.max(Math.max(x0,x1),x2),x3);
			int maxY = Math.max(Math.max(Math.max(y0,y1),y2),y3);
			//pos stores the tetrimino block position offsets at all 4 rotations; populated here in the constructor for the sake of simplicity
			pos = new int[][] {
				{x0,y0,x1,y1,x2,y2,x3,y3}, 
				{y0,maxX-x0,y1,maxX-x1,y2,maxX-x2,y3,maxX-x3}, 
				{maxX-x0,maxY-y0,maxX-x1,maxY-y1,maxX-x2,maxY-y2,maxX-x3,maxY-y3}, 
				{maxY-y0,x0,maxY-y1,x1,maxY-y2,x2,maxY-y3,x3}
			};
		}
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
    static Tetrimino curBlock = Tetrimino.O;
    static Tetrimino nextBlock = null;
    static boolean boardState[][] = new boolean[numRows][numCols];
    static int shadowCols[] = new int[4];
    static int shadowRows[] = new int[4];
    static int curShadowCol;
    static boolean boardPieceCopy[] = new boolean[4];
    //T-Spin detection
    static boolean setupFound = false;
    static int setupRotInd = 0;
    static int setupX = 0;
    static int setupY = 0;
    
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
           if (setupFound) {
        	   for (int i = 0; i < curBlock.pos[setupRotInd].length; i+=2) {
        		   g.setColor(Color.BLUE);
        		   g.fillRect(gridRight + 2*bSize + bSize*(setupY+curBlock.pos[setupRotInd][i+1]),gridTop + bSize*(setupX+curBlock.pos[setupRotInd][i]),bSize,bSize);
        	   }
           }
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
	
	/**
	 * search for any T-spins that may be created using the current tetrimino given the current board state
	 */
	public static void searchTSpinSetups() {
		int cx,cy;
		//if a T-spin is already present on the board, no need to look for an additional setup
		if (boardContainsTSpin(false,0,0,0)) {
			setupFound = false;
			return;
		}
		for (int i = 0; i < curBlock.pos.length; ++i) { //for all rotations
			for (int x = 0; x < numRows; ++x) { //for all rows
				boardIter:
					for (int y = 0; y < numCols; ++y) { //for all columns
						//check all block positions for validity
						boolean touchingGround = false;
						for (int j = 0; j < curBlock.pos[i].length; j+=2) {
							cx = x + curBlock.pos[i][j];
							cy = y + curBlock.pos[i][j+1];
							//ignore positions that put us partially out of bounds
							if (cx >= numRows || cy >= numCols) {
								continue boardIter;
							}
							//ignore positions that intersect already placed blocks
							if (boardState[cx][cy]) {
								continue boardIter;
							}
							//check if this block is on top of another block or the ground
							if (cx == numRows-1 || boardState[cx+1][cy]) {
								touchingGround = true;
							}
						}
						//make sure at least one block is on top of another block or the ground
						if (!touchingGround) {
							continue boardIter;
						}
						//this is a valid placement candidate; check if this position produces a T-spin
						if (boardContainsTSpin(true,x,y,i)) {
							setupFound = true;
							setupX = x;
							setupY = y;
							setupRotInd = i;
							return;
						}
					}
			}
		}
		setupFound = false;
	}
	
	/**
	 * determine whether or not the board contains a T-Spin, optionally including the specified position and rotation of the current block
	 * @param useNewBlock (boolean): whether to consider the specified block information (true) or just the board state as-is (false)
	 * @param x (int): the row in which to test the current block
	 * @param y (int): the column in which to test the current block
	 * @param rotInd (int): the index in the rotation array in which to test the current block
	 * @returns: whether the board contains a T-Spin (true) or not (false)
	 */
	private static boolean boardContainsTSpin(boolean useNewBlock, int x, int y, int rotInd) {
		//TODO: checking the whole board is pretty inefficient. This can easily be optimized to just check directly around the new block
		if (useNewBlock) { //temporarily copy the block positions onto the board
			boardPieceCopy[0] = boardState[x+curBlock.pos[rotInd][0]][y+curBlock.pos[rotInd][1]];
			boardState[x+curBlock.pos[rotInd][0]][y+curBlock.pos[rotInd][1]] = true;
			
			boardPieceCopy[1] = boardState[x+curBlock.pos[rotInd][2]][y+curBlock.pos[rotInd][3]];
			boardState[x+curBlock.pos[rotInd][2]][y+curBlock.pos[rotInd][3]] = true;
			
			boardPieceCopy[2] = boardState[x+curBlock.pos[rotInd][4]][y+curBlock.pos[rotInd][5]];
			boardState[x+curBlock.pos[rotInd][4]][y+curBlock.pos[rotInd][5]] = true;
			
			boardPieceCopy[3] = boardState[x+curBlock.pos[rotInd][6]][y+curBlock.pos[rotInd][7]];
			boardState[x+curBlock.pos[rotInd][6]][y+curBlock.pos[rotInd][7]] = true;
		}
		boolean foundTSpin = false;
		baseIter:
			for (int i = 0; i < numRows-2; ++i) {
				for (int r = 0; r < numCols-2; ++r) {
					if (!boardState[i+1][r] && !boardState[i+1][r+1] && !boardState[i+1][r+2] && !boardState[i][r+1] && !boardState[i+2][r+1] && //center cross should be free
							boardState[i+2][r] && boardState[i+2][r+2] && //bottom left and bottom right corners should be occupied
							(boardState[i][r] ^ boardState[i][r+2])) { //either topleft or topright should be occupied, but not both 
						foundTSpin = true;
						break baseIter;
					}
				}
			}
		if (useNewBlock) { //revert our board modifications from the copies we made earlier
			boardState[x+curBlock.pos[rotInd][0]][y+curBlock.pos[rotInd][1]] = boardPieceCopy[0];
			boardState[x+curBlock.pos[rotInd][2]][y+curBlock.pos[rotInd][3]] = boardPieceCopy[1];
			boardState[x+curBlock.pos[rotInd][4]][y+curBlock.pos[rotInd][5]] = boardPieceCopy[2];
			boardState[x+curBlock.pos[rotInd][6]][y+curBlock.pos[rotInd][7]] = boardPieceCopy[3];
		}
		return foundTSpin;
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
			searchTSpinSetups();
			
			//re-render and wait for the next frame
            tutor.repaint();
            frameTime  = (System.nanoTime() - time)/1000000;
            long sleepTime = (1000/fps) - frameTime;
            if (sleepTime > 0) 
            	Thread.sleep(sleepTime);
        }
    }    
}