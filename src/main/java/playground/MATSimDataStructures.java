package playground;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

public class MATSimDataStructures {
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Network network = scenario.getNetwork();
        NetworkFactory factory = network.getFactory();
        Node node1 = factory.createNode(Id.createNodeId(1l), CoordUtils.createCoord(0d, 0d));
        network.addNode(node1);
        Node node2 = factory.createNode(Id.createNodeId(2l), CoordUtils.createCoord(0d, 1d));
        network.addNode(node2);
        Node node3 = factory.createNode(Id.createNodeId(3l), CoordUtils.createCoord(1d, 1d));
        network.addNode(node3);
        Node node4 = factory.createNode(Id.createNodeId(4l), CoordUtils.createCoord(1d, 0d));
        network.addNode(node4);
        System.out.println();
        Link link = factory.createLink(Id.createLinkId(1), node1, node2);
        link.getAttributes().putAttribute("width",12.5);
        network.addLink(link);
        new NetworkWriter(scenario.getNetwork()).write("test.xml");

    }
}
