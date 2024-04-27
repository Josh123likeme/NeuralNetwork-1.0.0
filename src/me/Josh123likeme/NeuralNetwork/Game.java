package me.Josh123likeme.NeuralNetwork;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.Random;

import me.Josh123likeme.NeuralNetwork.InputListener.*;

public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public static final int INITIAL_WIDTH = 400, INITIAL_HEIGHT = 400;
	
	private Thread thread;
	private boolean running = false;
	
	private MouseWitness mouseWitness;
	private KeyboardWitness keyboardWitness;
	
	private double deltaFrame;
	private int fps;
	
	private final int NUMBER_OF_PLANTS = 100;
	private final int MAX_SPOT_SIZE = 100;
	private final int MAX_SPIKE_SIZE = 100;
	
	private double[] plantSpots; //x
	private double[] plantSpikes; //y
	private boolean[] plantSafe;
	
	private boolean[] identifiedAsSafe;
	
	private NeuralNetwork bestNeuralNetwork;
	private double bestCost;
	private int iteration = 0;
	private long lastInterationPrintTime;
	
	private final int DOT_SIZE = 6;
	
	public Game() {
		
		Random random = new Random();
		
		double x1 = random.nextDouble()*50 + 25;
		double y1 = random.nextDouble()*50 + 25;
		double m = random.nextDouble()*20 - 10;
		
		plantSpots = new double[NUMBER_OF_PLANTS];
		plantSpikes = new double[NUMBER_OF_PLANTS];
		plantSafe = new boolean[NUMBER_OF_PLANTS];
		
		for (int i = 0; i < NUMBER_OF_PLANTS; i++) {
			
			double x = random.nextDouble() * 100;
			double y = random.nextDouble() * 100;
			
			plantSpots[i] = x;
			plantSpikes[i] = y;
			
			//plantSafe[i] = (x-50)*(x-50) + (y-50)*(y-50) < 1000;
			plantSafe[i] = y < 2*(0.0003 * (x-20) * (- ((x-20) * (x-20)) + 60 * (x-20)) + 20);
			//plantSafe[i] = y < 50 - 0.7 * x;
			//plantSafe[i] = y < m * (x - x1) + y1;

		}
		
		bestNeuralNetwork = new NeuralNetwork(new int[] {2, 3, 2});
		bestCost = Double.MAX_VALUE;
		
		identifiedAsSafe = new boolean[NUMBER_OF_PLANTS];
		
		new Window(INITIAL_WIDTH, INITIAL_HEIGHT, "Neural Network", this);
		
		initInputs();
		
	}
	
	private void initInputs() {
		
		mouseWitness = new MouseWitness();
		keyboardWitness = new KeyboardWitness();
		
		addMouseListener(mouseWitness);
		addMouseMotionListener(mouseWitness);
		addKeyListener(keyboardWitness);
		
		requestFocus();
		
	}
	
	public synchronized void start() {
		
		thread = new Thread(this);
		thread.start();
		running = true;
		
	}
	
	public synchronized void stop() {
		
		try 
		{
			thread.join();
			running = false;
		}
		
		catch(Exception e) {e.printStackTrace();}
		
	}
	
	public void run() {
		
		double targetfps = 10000d;
		long targetDeltaFrame = Math.round((1d / targetfps) * 1000000000);
		long lastSecond = System.nanoTime();
		int frames = 0;
		
		long lastFrame = 0;
		
		while (running) {
			
			frames++;
			
			if (lastSecond + 1000000000 < System.nanoTime()) {
				
				fps = frames;
				
				frames = 0;
				
				lastSecond = System.nanoTime();
				
				targetDeltaFrame = Math.round((1d / targetfps) * 1000000000);
				
			}
			
			//starting to push frame
			
			long nextTime = System.nanoTime() + targetDeltaFrame;
			
			deltaFrame = ((double) (System.nanoTime() - lastFrame)) / 1000000000;
			
			lastFrame = System.nanoTime();
			
			update();
			
			paint();
			
			keyboardWitness.purgeTypedKeys();
			mouseWitness.purgeClickedButtons();
			
			//finished pushing frame
			
			while (nextTime > System.nanoTime());
			
		}
		stop();
		
	}
	
	private void update() {
		
		while (true) {
		
			iteration++;
			
			if (lastInterationPrintTime + 1000000000 < System.nanoTime()) {
				
				lastInterationPrintTime = System.nanoTime();
				
				System.out.println("iteration: " + iteration);
				
				break;
				
			}
			
			NeuralNetwork neuralNetwork = bestNeuralNetwork.clone();
			
			neuralNetwork.mutate(0.1, 0.1);
			
			double totalCost = 0;
			
			for (int i = 0; i < NUMBER_OF_PLANTS; i++) {
				
				double[] output = neuralNetwork.feedForward(new double[] {plantSpots[i], plantSpikes[i]});
				
				identifiedAsSafe[i] = output[0] > output[1];
				
				if (plantSafe[i]) {
					
					totalCost += Math.abs(output[0] - 1) + Math.abs(output[1]);
					
					//System.out.println("safe:" + output[0] + ":" + output[1] + " cost:" + (Math.abs(output[0] - 1) + Math.abs(output[1])));
					
				}
				else {
					
					totalCost += Math.abs(output[0]) + Math.abs(output[1] - 1);
					
					//System.out.println("badd:" + output[0] + ":" + output[1] + " cost:" + (Math.abs(output[0]) + Math.abs(output[1] - 1)));
					
				}
				
			}
			
			int numberCorrect = 0;
			
			for (int i = 0; i < plantSafe.length; i++) {
				
				if (plantSafe[i] == identifiedAsSafe[i]) numberCorrect++;
				
			}
			
			totalCost += 10*(NUMBER_OF_PLANTS - numberCorrect);
			
			if (totalCost < bestCost) {
				
				bestNeuralNetwork = neuralNetwork.clone();
				bestCost = totalCost;
				
				System.out.println("total cost:" + totalCost);
				
				System.out.println("number correct:" + numberCorrect + "/" + NUMBER_OF_PLANTS);
				
				break;
				
			}
			
		}
		
	}

	private void paint() {
	
		BufferStrategy bufferStrategy = this.getBufferStrategy();
		if(bufferStrategy == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics graphics = bufferStrategy.getDrawGraphics();
		
		//basic black background to stop flashing
		graphics.setColor(Color.black); 
		graphics.fillRect(0, 0, getWidth(), getHeight());
		
		//put rendering stuff here
		
		double xScale = (double) getWidth() / MAX_SPOT_SIZE;
		double yScale = (double) getHeight() / MAX_SPIKE_SIZE;
		
		int boundryDisplayPixelSize = 2;
		
		for (int y = 0; y < getHeight(); y += boundryDisplayPixelSize) {
			
			for (int x = 0; x < getWidth(); x += boundryDisplayPixelSize) {
				
				double[] output = bestNeuralNetwork.feedForward(new double[] {x / xScale,  (getHeight() - y) / yScale});
				
				int colour;
				
				//safe
				if (output[0] > output[1]) colour = 0xFF008000;
				else colour = 0xFF800000;
				
				graphics.setColor(new Color(colour));
				graphics.fillRect(x, y, boundryDisplayPixelSize, boundryDisplayPixelSize);
				
			}
			
		}
		
		for (int i = 0; i < NUMBER_OF_PLANTS; i++) {
			
			int colour;
			
			if (plantSafe[i]) colour = 0xFF00FF00;
			else colour = 0xFFFF0000;
			
			graphics.setColor(new Color(colour));
			
			graphics.fillOval((int) (plantSpots[i] * xScale) - DOT_SIZE / 2,
			   getHeight() - (int) (plantSpikes[i] * yScale) - DOT_SIZE / 2,
			   DOT_SIZE, DOT_SIZE);
			
		}
		
		//this pushes the graphics to the window
		bufferStrategy.show();
		
	}
	
}
