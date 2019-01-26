package app.main.src;

import java.awt.Dimension;

import javax.swing.JFrame;

public class Display {
	
	public static JFrame create(int width, int height, String title) {
		
		JFrame frame = new JFrame(title);
		
		
		
		frame.getContentPane().setPreferredSize(new Dimension(width, height));
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.revalidate();
		frame.repaint();
		
		return frame;
		
	}
	
	
}
