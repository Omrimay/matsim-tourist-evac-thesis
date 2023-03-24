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

public class TouristPathSimulatorMin {
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

    /**
     * In order to be consistent with the clockwise listing of angles in {@link NetworkUtils}
     * getOutLinksSortedClockwiseByAngle,
     * we return the negative value of the arctan
     *
     * @param origin
     * @param destination
     *
     * @return value in radians, clockwise = positive
     */
    public static double getTargetAngle(Node origin, Node destination) {
        double x = destination.getCoord().getX() - origin.getCoord().getX();
        double y = destination.getCoord().getY() - origin.getCoord().getY();
        double thetaInLink = Math.atan2(y, x);
        return thetaInLink;
    }

    double getEuclideanDistanceFactor(double startDistance, double currentDistance) {
        double euclideanDistanceFactor = (startDistance - currentDistance) / startDistance;
        if (euclideanDistanceFactor < 0)
            euclideanDistanceFactor = 0;
        if (euclideanDistanceFactor > 1)
            euclideanDistanceFactor = 1;
        return euclideanDistanceFactor;
    }

    public LeastCostPathCalculator.Path simulatePath(Link fromLink, Link toLink, double starttime, Person person, Vehicle vehicle) {
        AbstractSequentialList<Node> nodes = new LinkedList<>();
        AbstractSequentialList<Link> links = new LinkedList<>();

        nodes.add(fromLink.getFromNode());
        links.add(fromLink);
        Link currentLink = fromLink;
        final Link newCurntLink = currentLink;
        final Link newToLink = toLink;
        double cost = 0d;
        double travelTime = 0d;

        while (!currentLink.equals(toLink)) {

            RandomCollection<Link> linkSampler = new RandomCollection<>();
            TreeMap<Double, Link> options = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink);
            options.entrySet().forEach(e -> {
                double targetRadians = getTargetAngle(newToLink.getToNode(), e.getValue().getFromNode());
                double linkDirection  = getTargetAngle(e.getValue().getToNode(), e.getValue().getFromNode());
                if (e.getValue().getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (e.getValue().getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        linkDirection = linkDirection;
                    }
                    else {
                        linkDirection = + 3.06;
                    }
                }
                else {
                    if (e.getValue().getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        linkDirection = linkDirection;
                    } else {
                        linkDirection = +3.06;
                        linkDirection = linkDirection * -1;
                    }
                }
                if (newToLink.getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (newToLink.getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        targetRadians = targetRadians;
                    }
                    else {
                        targetRadians = + 3.06;
                    }
                }
                else {
                    if (newToLink.getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        targetRadians = targetRadians;
                    } else {
                        targetRadians = +3.06;
                        targetRadians = targetRadians * -1;
                    }
                }
                double howfar = Math.abs(linkDirection - targetRadians + random.nextDouble() * 1e-10);
                                        linkSampler.add(howfar, e.getValue());


                });

            // get a link

            Link choice = linkSampler.selectRandom();
            cost += Math.log(linkSampler.getWeight(choice));
            travelTime += choice.getLength(); //assume 1m/s for now
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