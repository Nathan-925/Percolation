import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//This probably wont run with default stack size i used -Xss1G but i think it only uses ~250mb at normal size
public class Percolator {
	
	public static void main(String[] args) {
		int width = args.length >= 2 ? Integer.parseInt(args[0]) : 800, height = args.length >= 2 ? Integer.parseInt(args[1]) : 600;
		Random rand = args.length >= 3 ? new Random(Long.parseLong(args[2])) : new Random();
		long colorSeed = rand.nextLong();
		double edges[][] = new double[width*height][4];
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++){
				edges[i+j*width][0] = rand.nextDouble();
				if(i > 0)
					edges[i-1+j*width][2] = edges[i+j*width][0];
				edges[i+j*width][1] = rand.nextDouble();
				if(j > 0)
					edges[i+(j-1)*width][3] = edges[i+j*width][1];
			}
		
		JSlider slider = new JSlider(SwingConstants.VERTICAL, 0, 1000, 0);
		Hashtable<Integer, JLabel> dic = new Hashtable<>();
		dic.put(0, new JLabel("0"));
		dic.put(500, new JLabel("0.5"));
		dic.put(1000, new JLabel("1"));
		slider.setLabelTable(dic);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		
		JFrame frame = new JFrame("Percolation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new GridBagLayout());
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		JPanel panel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, height);
			}
			
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img, 0, 0, null);
			}
		};
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double p = (double)slider.getValue()/(slider.getMaximum()-slider.getMinimum());
				boolean check[] = new boolean[width*height];
				int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
				rand.setSeed(colorSeed);
				for(int i = 0; i < width; i++)
					for(int j = 0; j < height; j++)
						color(i, j, p, pixels, check, rand.nextInt(0xFFFFFF)+1);
				panel.repaint();
			}
			
			private void color(int x, int y, double p, int[] pixels, boolean[] check, int color) {
				if(x < 0 || x >= width || y < 0 || y >= height || check[x+y*width])
					return;
				pixels[x+y*width] = color;
				check[x+y*width] = true;
				double arr[] = edges[x+y*width];
				for(int i = 0; i < 4; i++)
					if(arr[i] < p)
						color(x-(i-1)%2, y-(i-2)%2, p, pixels, check, color);
			}
		});
		slider.getChangeListeners()[0].stateChanged(null);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.VERTICAL;
		frame.getContentPane().add(panel, constraints);
		frame.getContentPane().add(slider, constraints);
		frame.pack();
		frame.setVisible(true);
	}
	
}
