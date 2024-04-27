package me.Josh123likeme.NeuralNetwork;

public class NeuralNetwork {
	
	private Layer[] layers;
	
	private NeuralNetwork() { }
	
	public NeuralNetwork(int[] layerNodeCounts) {
		
		layers = new Layer[layerNodeCounts.length - 1];
		
		for (int i = 0; i < layers.length; i++) {
			
			layers[i] = new Layer(layerNodeCounts[i], layerNodeCounts[i + 1]);
			
		}

	}
	
	public double[] feedForward(double[] inputs) {
		
		double outputs[] = inputs;
		
		for (Layer layer : layers) {
			
			outputs = layer.feedForward(outputs);
			
		}
		
		return outputs;
		
	}
	
	public void mutate(double weight, double cost) {
		
		for (Layer layer : layers) {
			
			layer.mutate(weight, cost);
			
		}
		
	}
	
	public NeuralNetwork clone() {
		
		NeuralNetwork neuralNetwork = new NeuralNetwork();
		
		neuralNetwork.layers = new Layer[layers.length];
		
		for (int i = 0; i < layers.length; i++) {
			
			neuralNetwork.layers[i] = layers[i].clone();
			
		}
		
		return neuralNetwork;
		
	}
	
}
