package org.matsim.core.router.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;

import java.util.AbstractSequentialList;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;

public class TouristPathSimulatortestRandom {
    private final Random random = MatsimRandom.getLocalInstance();

    static double getLinkWidth(Link link) {
        Object width = link.getAttributes().getAttribute("width");
        if (width != null) {
            try {
                return (double) width;
            } catch (Exception e) {
            }
        }
        // default
        return link.getNumberOfLanes() * 3;
    }

    public static double getTargetAngle(Node destination , Node origin) {
        double x = destination.getCoord().getX() - origin.getCoord().getX();
        double y = destination.getCoord().getY() - origin.getCoord().getY();
        double thetaInLink = Math.atan(y/x);
        return thetaInLink;
    }


    public LeastCostPathCalculator.Path simulatePath(Link fromLink, Link toLink, double starttime, Person person, Vehicle vehicle) {
        AbstractSequentialList<Node> nodes = new LinkedList<>();
        AbstractSequentialList<Link> links = new LinkedList<>();

        nodes.add(fromLink.getFromNode());
        links.add(fromLink);
        Link currentLink = fromLink;
        Link newToLink = toLink;
        double cost = 0d;
        double travelTime = 0d;
        while (!currentLink.equals(toLink)) {
            double targetRadians = getTargetAngle(currentLink.getToNode(), toLink.getFromNode());
            RandomCollection<Link> linkSampler = new RandomCollection<>();
            RandomCollection<Link> linkSamplerMin = new RandomCollection<>();
            Link back = NetworkUtils.findLinkInOppositeDirection(currentLink);
            if (back != null) {
                linkSampler.add(1, back);
            }

            TreeMap<Double, Link> options = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink);
            options.entrySet().forEach(e -> {
                double targetRadiansNew = getTargetAngle(newToLink.getToNode(), e.getValue().getFromNode());
                double linkDirection  = getTargetAngle(e.getValue().getToNode(), e.getValue().getFromNode());
                if (e.getValue().getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (e.getValue().getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        linkDirection = 1.57 - linkDirection;
                    }
                    else {
                        linkDirection = 4.71 - linkDirection;
                    }
                }
                else {
                    if (e.getValue().getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        linkDirection = 1.57 - linkDirection;
                    } else {
                        linkDirection = 4.71 - linkDirection;
                    }
                }
                if (newToLink.getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (newToLink.getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        targetRadiansNew = 1.57 - targetRadiansNew;
                    }
                    else {
                        targetRadiansNew = 4.71 - targetRadiansNew;
                    }
                }
                else {
                    if (newToLink.getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        targetRadiansNew = 1.57 - targetRadiansNew;
                    } else {
                        targetRadiansNew = 4.71 - targetRadiansNew;
                    }
                }
                double howfar = 0;
                if (targetRadiansNew>linkDirection){
                    if(targetRadiansNew-linkDirection<6.28-targetRadiansNew+linkDirection) {
                        howfar = targetRadiansNew - linkDirection;
                    }else {
                        howfar = 6.28-targetRadiansNew+linkDirection;
                    }
                }else
                if(linkDirection-targetRadiansNew<6.28-linkDirection+targetRadiansNew){
                    howfar = linkDirection - targetRadiansNew;
                }else{
                    howfar = 6.28-linkDirection+targetRadiansNew;
                }
                linkSamplerMin.add(howfar+0.001, e.getValue());


            });

            Link choice = fromLink;
            // get a link
            if (options.size() >= 1) {
                choice = linkSamplerMin.selectRandomWalk();
            } else {
                choice = linkSampler.select();
            }
            links.add(choice);
            nodes.add(choice.getFromNode());
            currentLink = choice;
        }
        nodes.add(currentLink.getToNode());
        links.remove(0);
        links.remove(links.size() - 1);


        return new LeastCostPathCalculator.Path(nodes, links, travelTime, cost);
    }
}
