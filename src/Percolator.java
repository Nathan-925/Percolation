import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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

public class Percolator {

    private int width, height;
    private double probability;
    private double[][] edges;
    private int[] colors;
    private BufferedImage img;
    
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
        
        colors = new int[edges.length];
        for(int i = 0; i < colors.length; i++)
            colors[i] = rand.nextInt(0x1000000);
        
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        setProbability(probability);
    }
    
    public Percolator(int width, int height, double probability) {
        this(width, height, probability, new Random());
    }
    
    public Percolator(int width, int height, Random rand) {
        this(width, height, 0, rand);
    }
    
    public Percolator(int width, int height) {
        this(width, height, 0);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public double getProbability() {
        return probability;
    }
    
    public void setProbability(double probability) {
        this.probability = probability;
        boolean check[] = new boolean[width * height];
        int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < width * height; i++)
            if (!check[i]) {
                Deque<Integer> stack = new LinkedList<>();
                stack.push(i);
                while (!stack.isEmpty()) {
                    int n = stack.pop();
                    pixels[n] = colors[i];
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
    
    public BufferedImage getImage() {
        return img;
    }
    
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
