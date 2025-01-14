package core.level.levelgraph;

import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;

import dslToGame.ConvertedGraph;
import dslToGame.DotToLevelGraph;
import dslToGame.graph.Graph;
import dslToGame.graph.Node;

import java.util.HashMap;

/**
 * Generates a space-based level whose structure is defined by the given graph
 *
 * @quthor Andre Matutat
 */
public class GraphLevelGenerator implements IGenerator {

    private LevelNode root;
    private Graph<String> graph;
    public static HashMap<Node<String>, LevelNode> nodeToLevelNode;
    public static HashMap<LevelNode, Node<String>> levelNodeToNode;

    public GraphLevelGenerator(Graph<String> graph) {
        graph(graph);
    }

    /**
     * The Root-Node defines the graph
     *
     * @param graph
     */
    public void graph(Graph<String> graph) {
        ConvertedGraph cg = DotToLevelGraph.convert(graph);
        root = cg.root();
        this.graph = cg.graph();
        nodeToLevelNode = cg.nodeToLevelNode();
        levelNodeToNode = cg.levelNodeToNode();
    }

    @Override
    public ILevel level(DesignLabel designLabel, LevelSize size) {
        if (root == null)
            throw new NullPointerException("Root is null. Please add a graph to this generator");
        else return (ILevel) new GraphLevel(root, size, designLabel).rootRoom();
    }

    @Override
    public LevelElement[][] layout(LevelSize size) {
        throw new UnsupportedOperationException("This Method is not supported for GraphLevel");
    }

    public Graph<String> graph() {
        return graph;
    }

    public HashMap<Node<String>, LevelNode> getNodeToLevelNode() {
        return nodeToLevelNode;
    }

    public HashMap<LevelNode, Node<String>> getLevelNodeToNode() {
        return levelNodeToNode;
    }
}
