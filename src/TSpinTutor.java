import java.awt.*;
import java.util.Random;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TSpinTutor extends JFrame {
	//global random generator
	static Random r = new Random();
	//graphics constants
	static int sw = 1920;
	static int sh = 1080;
	static int bSize = 36;
	static int bSizePrev = 28;
	static int bSizePrevFuture = 22;
	static int gridLeft = 308;
	static int gridRight = 632;
	static int gridTop = 160;
	static int gridBot = 844;
	static int numRows = 20;
	static int numCols = 10;
	//timing vars
	public static long time = 0;
    public static int fps = 30;
	
	TSpinTutor() {
		//set the frame properties for a transparent, always on top overlay
        setUndecorated(true);
        setSize(sw, sh);
        setLocation(0, 0);
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        setBackground(new Color(0,0,0,0));
	}
	
	public static class Panel extends JPanel {
        public Panel() {
            setBackground(new Color(0,0,0,0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int x = gridLeft + bSize*r.nextInt(numCols);
    		int y = gridTop + bSize*r.nextInt(numRows);
    		g.setColor(Color.RED);
    		g.fillRect(x,y,bSize,bSize);
        }
    }
	
	public static void main(String[] args) {
        final TSpinTutor tutor = new TSpinTutor();   
        Panel panel = new Panel();
        tutor.add(panel);
        tutor.setVisible(true);
        while(true) {
        	time = System.nanoTime();
            tutor.repaint();
            time = System.nanoTime() - time;
            try {Thread.sleep( (1000/fps) - (time/1000000) );} catch (Exception e){}
        }
    }    
}