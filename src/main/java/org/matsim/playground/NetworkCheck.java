package org.matsim.playground;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NetworkCheck {
    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork(ConfigUtils.createConfig());
        new MatsimNetworkReader(network).readFile("dropbox/network3.xml");

        collapseNodes(network, 15);
        new NetworkWriter(network).write("temp/netwrok_15.xml");

        NetworkCleaner nc = new NetworkCleaner();
        nc.run(network);
        System.out.println();
        new NetworkWriter(network).write("temp/netwrok_15_clean.xml");


    }

    public static void collapseNodes(Network network, double radius) {
        {
            Collection<? extends Node> nodes = network.getNodes().values();
            Set<Coord> nodeCoords = new HashSet<>();
            for (Node node : nodes) {
                nodeCoords.add(node.getCoord());
            }
            long idcount = 0l;

            for (Coord nodeCoord : nodeCoords) {
                Collection<Node> nearestNodes = NetworkUtils.getNearestNodes(network, nodeCoord, radius);
                double sumx = 0d, sumy = 0d;
                int count = 0;
                for (Node nearestNode : nearestNodes) {
                    count++;
                    sumx += nearestNode.getCoord().getX();
                    sumy += nearestNode.getCoord().getY();
                }
                Coord coord = CoordUtils.createCoord(sumx / count, sumy / count);
                Node centroid = network.getFactory().createNode(Id.createNodeId("n_" + idcount++), coord);
                network.addNode(centroid);

                for (Node node : nearestNodes) {
                    for (Link link : node.getInLinks().values()) {
                        link.setToNode(centroid);
                        centroid.addInLink(link);
                        node.removeInLink(link.getId());
                    }
                    for (Link link : node.getOutLinks().values()) {
                        link.setFromNode(centroid);
                        centroid.addOutLink(link);
                        node.removeOutLink(link.getId());
                    }
                }
            }
        }

        //FINALLY, REMOVE ZERO LENGTH LINKS
        {
            Set<Link> badlinks = new HashSet<>();
            for (Link link : network.getLinks().values()) {
                if (link.getToNode().equals(link.getFromNode()))
                    badlinks.add(link);
                if (link.getLength() <= 0)
                    badlinks.add(link);

            }
            for (Link badlink : badlinks) {
                network.removeLink(badlink.getId());
            }
            Set<Node> badNodes = new HashSet<>();
            for (Node node : network.getNodes().values()) {
                if (node.getInLinks().size() == 0 || node.getOutLinks().size() == 0)
                    badNodes.add(node);
            }
            badNodes.forEach(node -> network.removeNode(node.getId()));
        }
    }
}
