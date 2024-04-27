package me.Josh123likeme.NeuralNetwork;

import java.util.Random;

public class Layer {
	
	private static final Random random = new Random();
	
	private double[][] weights;
	private double[] bias;
	
	private Layer() { }
	
	public Layer(int numberOfInputs, int numberOfOutputs) {
		
		weights = new double[numberOfOutputs][numberOfInputs];
		bias = new double[numberOfOutputs];
		
		for (int row = 0; row < weights.length; row++) {
			
			for (int col  = 0; col < weights[0].length; col++) {
				
				weights[row][col] = random.nextDouble() * 2 - 1;
				
			}
			
			bias[row] = random.nextDouble() * 2 - 1;
			
		}
		
	}
	
	public double[] feedForward(double[] inputs) {
		
		//apply matrix multiplication, add bias and use activation function
		
		double[] outputs = new double[weights.length];
		
		for (int row = 0; row < weights.length; row++) {
			
			double total = 0;
			
			for (int col = 0; col < weights[0].length; col++) {
				
				
				total += weights[row][col] * inputs[col];
				
			}
			
			outputs[row] = activationFunction(total + bias[row]);
			
		}
		
		return outputs;

	}
	
	private double activationFunction(double value) {
		
		return 1 / (1 + Math.exp(-value));
		
	}
	
	public void mutate(double weight, double cost) {
		
		for (int row = 0; row < weights.length; row++) {
			
			for (int col  = 0; col < weights[0].length; col++) {
				
				weights[row][col] += random.nextDouble() * 2 * weight - weight;
				
			}
			
			bias[row] += random.nextDouble() * 2 * cost - cost;
			
		}
		
	}
	
	public Layer clone() {
		
		Layer layer = new Layer();
		
		layer.weights = new double[weights.length][weights[0].length];
		layer.bias = new double[bias.length];
		
		for (int row = 0; row < weights.length; row++) {
			
			for (int col  = 0; col < weights[0].length; col++) {
				
				layer.weights[row][col] = weights[row][col];
				
			}
			
			layer.bias[row] = bias[row];
			
		}
		
		return layer;
		
	}
	
}
