// By: Daniyaal Farooqi and Aswin Kuganesan
// December 17, 2019
// PACMAN
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class pacmanMain extends JPanel implements KeyListener, ActionListener{
	
	public static JFrame mainFrame;
	public static JPanel mainPanel;
	
	public pacmanMain(){
		mainFrame = new JFrame("Main Menu");
        mainFrame.setPreferredSize(new Dimension(800, 800));
        mainFrame.setLocation(200, 200);
        mainPanel = new JPanel();
	}
	
	public static void main(String[] args) {
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
