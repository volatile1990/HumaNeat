package de.humaneat.core.neat.genome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.humaneat.core.global.Random;
import de.humaneat.core.global.components.connection.ConnectionHistory;
import de.humaneat.core.global.components.node.NodeGeneType;
import de.humaneat.core.global.genome.DefaultGenomeManager;
import de.humaneat.core.neat.Property;
import de.humaneat.core.neat.genes.connection.ConnectionGene;
import de.humaneat.core.neat.genes.node.NodeGene;

/**
 * @author MannoR
 *
 */
public class GenomeManager implements DefaultGenomeManager {

	public Genome genome;

	/**
	 * @param genome
	 */
	public GenomeManager(Genome genome) {
		this.genome = genome;
	}

	/**
	 * Generates all inputs and outputs
	 * Links all inputs to all outputs
	 * Creates the bias nodes and links it to all generated outputs
	 *
	 * @param genome
	 */
	@Override
	public void initialize() {

		// Connect all inputs to all outputs
		connectInputsAndOutputs(generateInputs(), generateOutputs());

		// Add bias node
		generateAndConnectBias();

		// Generate initial network for feed forward
		genome.getLinker().generateNetwork();
	}

	/**
	 * Generates all input nodes
	 *
	 * @param genome
	 * @return
	 */
	private List<NodeGene> generateInputs() {

		List<NodeGene> inputNodes = new ArrayList<>();
		for (int i = 0; i < genome.anzInputs; ++i) {

			// Get next node innovation number
			int nextInnovation = genome.nodeInnovation.getNext();

			// Create input node
			NodeGene input = new NodeGene(NodeGeneType.INPUT, nextInnovation);

			// Add input node
			genome.nodes.put(nextInnovation, input);
			inputNodes.add(input);
		}

		return inputNodes;
	}

	/**
	 * Generates all output nodes
	 *
	 * @param genome
	 * @return
	 */
	private List<NodeGene> generateOutputs() {

		List<NodeGene> outputNodes = new ArrayList<>();
		for (int i = 0; i < genome.anzOutputs; ++i) {

			// Get next node innovatin number
			int nextInnovation = genome.nodeInnovation.getNext();

			// Create output node
			NodeGene output = new NodeGene(NodeGeneType.OUTPUT, nextInnovation);

			// Add output node
			genome.nodes.put(nextInnovation, output);
			outputNodes.add(output);
		}

		return outputNodes;
	}

	/**
	 * Connects all generated input and output nodes
	 *
	 * @param genome
	 * @param inputNodes
	 * @param outputNodes
	 * @return
	 */
	private void connectInputsAndOutputs(List<NodeGene> inputNodes, List<NodeGene> outputNodes) {

		for (NodeGene inputNode : inputNodes) {
			for (NodeGene outputNode : outputNodes) {

				double max = Property.WEIGHT_RANDOM_RANGE.getValue();
				double min = -1 * Property.WEIGHT_RANDOM_RANGE.getValue();
				double weight = Random.random(min, max);

				ConnectionGene connection = new ConnectionGene(inputNode, outputNode, weight, true, genome.connectionInnovation.getNext());
				genome.addConnectionGene(connection);
			}
		}
	}

	/**
	 * Generates the bias and connects it to all generated output nodes
	 *
	 * @param genome
	 * @param random
	 */
	private void generateAndConnectBias() {

		// Create and add bias node
		int biasInnovation = genome.nodeInnovation.getNext();
		genome.biasNode = new NodeGene(NodeGeneType.BIAS, biasInnovation);
		genome.nodes.put(biasInnovation, genome.biasNode);

		// Connect the bias node to all outputs
		for (NodeGene outputNode : genome.getNodesByType(NodeGeneType.OUTPUT)) {

			double max = Property.WEIGHT_RANDOM_RANGE.getValue();
			double min = -1 * Property.WEIGHT_RANDOM_RANGE.getValue();
			double weight = Random.random(min, max);

			ConnectionGene connection = new ConnectionGene(genome.biasNode, outputNode, weight, true, genome.connectionInnovation.getNext());
			genome.addConnectionGene(connection);
		}
	}

	/**
	 * Sets this genomes connection innovation to the next connection innovation number
	 */
	@Override
	public void updateConnectionInnovation() {

		int highestInnovation = 0;
		for (ConnectionGene connection : genome.connections.values()) {
			if (connection.innvoationNumber > highestInnovation) {
				highestInnovation = connection.innvoationNumber;
			}
		}

		genome.connectionInnovation.setCurrent(++highestInnovation);
	}

	/**
	 * Sets this genomes node innovation to the next node innovation number
	 */
	@Override
	public void updateNodeInnovation() {

		int highestInnovation = 0;
		for (NodeGene node : genome.nodes.values()) {
			if (node.innovationNumber > highestInnovation) {
				highestInnovation = node.innovationNumber;
			}
		}

		genome.nodeInnovation.setCurrent(++highestInnovation);
	}

	/**
	 * @param innovationHistory
	 * @param fromInnovationNumber
	 * @param toInnovationNumber
	 * @return the next innovationNumber for the connection between from and to
	 */
	@Override
	public int getConnectionInnovationNumber(Map<Integer, List<ConnectionHistory>> innovationHistory, int fromInnovationNumber, int toInnovationNumber) {

		boolean newConnection = true;
		int connectionInnovationNumber = genome.connectionInnovation.getCurrent() + 1;
		List<ConnectionHistory> possibleMatches = innovationHistory.get(genome.connections.size());
		if (possibleMatches != null) {
			for (int i = 0; i < possibleMatches.size(); ++i) {

				if (possibleMatches.get(i).matches(getInnovationNumbers(), fromInnovationNumber, toInnovationNumber)) {
					newConnection = false;
					return possibleMatches.get(i).innovationNumber;
				}
			}
		}

		if (newConnection) {

			ArrayList<Integer> innovationNumbers = new ArrayList<>();
			for (ConnectionGene connection : genome.connections.values()) {
				innovationNumbers.add(connection.innvoationNumber);
			}

			// Save new innovation history
			List<ConnectionHistory> existingHistories = innovationHistory.get(innovationNumbers.size());
			if (existingHistories == null) {
				existingHistories = new ArrayList<>();
			}
			existingHistories.add(new ConnectionHistory(fromInnovationNumber, toInnovationNumber, connectionInnovationNumber, innovationNumbers));
			innovationHistory.put(innovationNumbers.size(), existingHistories);

			genome.connectionInnovation.getNext();
		}

		return connectionInnovationNumber;
	}

	/**
	 * @return
	 */
	private List<Integer> getInnovationNumbers() {

		List<Integer> innovationNumbers = new ArrayList<>();
		for (ConnectionGene connection : genome.connections.values()) {
			innovationNumbers.add(connection.innvoationNumber);
		}

		return innovationNumbers;
	}

}
