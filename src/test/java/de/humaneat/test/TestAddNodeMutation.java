package de.humaneat.test;

import java.util.Random;

import de.humaneat.core.global.components.node.NodeGeneType;
import de.humaneat.core.neat.genes.Counter;
import de.humaneat.core.neat.genes.connection.ConnectionGene;
import de.humaneat.core.neat.genes.node.NodeGene;
import de.humaneat.core.neat.genome.Genome;

/**
 * @author muellermak
 *
 */
public class TestAddNodeMutation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Counter innovation = new Counter();
		Random random = new Random();

		Genome genome = new Genome();

		NodeGene input1 = new NodeGene(NodeGeneType.INPUT, -3);
		NodeGene input2 = new NodeGene(NodeGeneType.INPUT, -2);
		NodeGene output = new NodeGene(NodeGeneType.OUTPUT, -1);

		genome.addNodeGene(input1);
		genome.addNodeGene(input2);
		genome.addNodeGene(output);

		ConnectionGene con1 = new ConnectionGene(input1, output, 0.5f, true, -2);
		ConnectionGene con2 = new ConnectionGene(input2, output, 1f, true, -1);

		genome.addConnectionGene(con1);
		genome.addConnectionGene(con2);

//		genome.addNodeMutation(new Random());

		for (ConnectionGene connection : genome.connections.values()) {
			if (connection.enabled) {
				System.out.println("IN-NODE-ID: " + connection.from.innovationNumber + " ## OUT-NODE-ID: " + connection.to.innovationNumber + " ## WEIGHT: " + connection.weight);
			}
		}
	}
}