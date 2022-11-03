import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Deque;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A 2 dimensional grid where connections in cardinal directions have a random double from 0-1 associated.
 * Connections with a value less than the set probability are active.
 * Cells that have connections are part of the same group.
 * 
 * @author Nathaniel Dunlap
 * @version 11/03/2022
 *
 */
public class Percolator {

    private int width, height;
    private double probability;
    private double[][] edges;
    private int[] pixels;
    private int[] colors;
    private BufferedImage img;
    
    /**
     * Creates a Percolator with the given size and probability using the provided Random.
     * 
     * @param width width of the Percolator
     * @param height height of the Percolator
     * @param probability the probability that any given connection is active
     * @param rand the Random that will be used to construct the Percolator
     */
    public Percolator(int width, int height, double probability,  Random rand) {
        this.width = width;
        this.height = height;
        
        edges = new double[width * height][4];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                edges[i + j * width][0] = rand.nextDouble();
                if (i < width-1)
                    edges[i + 1 + j * width][2] = edges[i + j * width][0];
                edges[i + j * width][1] = rand.nextDouble();
                if (j < height-1)
                    edges[i + (j + 1) * width][3] = edges[i + j * width][1];
            }
        
        pixels = new int[edges.length];
        
        colors = new int[edges.length];
        for(int i = 0; i < colors.length; i++)
            colors[i] = rand.nextInt(0x1000000);
        
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        setProbability(probability);
    }
    
    /**
     * Creates a Percolator with the given size and probability.
     * 
     * @param width width of the Percolator
     * @param height height of the Percolator
     * @param probability the probability that any given connection is active
     */
    public Percolator(int width, int height, double probability) {
        this(width, height, probability, new Random());
    }
    
    /**
     * Creates a Percolator with the given size with a probability of 0 using the provided Random.
     * 
     * @param width width of the Percolator
     * @param height height of the Percolator
     * @param rand the Random that will be used to construct the Percolator
     */
    public Percolator(int width, int height, Random rand) {
        this(width, height, 0, rand);
    }
    
    /**
     * Creates a Percolator with the given size.
     * 
     * @param width width of the Percolator
     * @param height height of the Percolator
     */
    public Percolator(int width, int height) {
        this(width, height, 0);
    }
    
    /**
     * Returns the width of the Percolator.
     * @return the width of the Percolator
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Returns the height of the Percolator.
     * @return the height of the Percolator
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Returns the probability that any given connection is active.
     * @return the probability that any given connection is active
     */
    public double getProbability() {
        return probability;
    }
    
    /**
     * Sets the probability that any given connection is active and finds which cells are connected.
     * @param probability the probability that any given connection is active
     */
    public void setProbability(double probability) {
        this.probability = probability;
        boolean check[] = new boolean[width * height];
        for (int i = 0; i < width * height; i++)
            if (!check[i]) {
                Deque<Integer> stack = new LinkedList<>();
                stack.push(i);
                while (!stack.isEmpty()) {
                    int n = stack.pop();
                    pixels[n] = i;
                    check[n] = true;
                    double arr[] = edges[n];
                    for (int j = 0; j < 4; j++) {
                        int next = n - ((j - 1) % 2) - width * ((j - 2) % 2);
                        if (next >= 0 && next < pixels.length && !check[next] && arr[j] < probability)
                            stack.push(next);
                    }
                }
            }
    }
    
    /**
     * Returns an int representing which group the given cell is a part of.
     * The int returned is arbitrary. 
     * The only guarantee is that cells in the same group will return the same int and cells in different groups will return different ints.
     * 
     * @param x x coordinate of the cell
     * @param y y coordinate of the cell
     * @return an int unique to the group the cell is in
     */
    public int getGroup(int x, int y) {
        return pixels[x+y*width];
    }
    
    /**
     * Creates an image representing the Percolator, where groups are defined by random colors.
     * @return an image representing the Percolator
     */
    public BufferedImage getImage() {
        Arrays.setAll(((DataBufferInt)img.getRaster().getDataBuffer()).getData(), n -> colors[pixels[n]]);
        return img;
    }
    
	/**
	 * Creates a JFrame with a slider that can be used to adjust the probability of the Percolator.
	 * Up to three arguments may be used specifying, in order, the width, height, and random seed of the Percolator.
	 * By default a width of 800, a height of 600, and a random seed is used.
	 * 
	 * @param args optional arguments specifying width, height, and a seed
	 */
	public static void main(String[] args) {
		int width = args.length >= 2 ? Integer.parseInt(args[0]) : 800,
				height = args.length >= 2 ? Integer.parseInt(args[1]) : 600;
		Random rand = args.length >= 3 ? new Random(Long.parseLong(args[2])) : new Random();
		
		Percolator perc = new Percolator(width, height, rand);
		
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
		slider.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 5));

		JFrame frame = new JFrame("Percolation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLayout(new GridBagLayout());

		JPanel panel = new JPanel() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(width, height);
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(perc.getImage(), 0, 0, null);
			}
		};
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				perc.setProbability((double)slider.getValue()/(slider.getMaximum()-slider.getMinimum()));
				panel.repaint();
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
