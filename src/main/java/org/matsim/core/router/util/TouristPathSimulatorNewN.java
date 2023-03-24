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

import static org.matsim.core.router.util.TouristChoiceCoefficients.*;

public class TouristPathSimulatorNewN {
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
        return -thetaInLink;
    }

    double calcLinkUtil(double ASC, double width, double feta_aD) {
        return Math.exp(ASC + widthCoeficient * width + beta_D * feta_aD + random.nextDouble() * 1e-10);
    }

    double calcLinkUtil(double ASC, double width, double feta_aD, double euclideanDistanceFactor) {

        return Math.exp(ASC + widthCoeficient * width + beta_D * feta_aD * euclideanDistanceFactor + random.nextDouble() * 1e-10);
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
        Link newToLink = toLink;
        double cost = 0d;
        double travelTime = 0d;
        double euclideanDistanceFromOriginToDestination = NetworkUtils.getEuclideanDistance(fromLink.getFromNode().getCoord(), toLink.getToNode().getCoord());
        while (!currentLink.equals(toLink)) {
            double euclideanDistanceToDestination = NetworkUtils.getEuclideanDistance(currentLink.getToNode().getCoord(), toLink.getToNode().getCoord());
            double targetRadians = getTargetAngle(currentLink.getToNode(), toLink.getFromNode());
            RandomCollection<Link> linkSampler = new RandomCollection<>();
            RandomCollection<Link> linkSamplerMin = new RandomCollection<>();
            Link back = NetworkUtils.findLinkInOppositeDirection(currentLink);
            if (back != null) {
                linkSampler.add(calcLinkUtil(ASC_Back, getLinkWidth(back), 0d), back);
            }

            TreeMap<Double, Link> options = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink);
            options.entrySet().forEach(e -> {
                double targetRadiansNew = getTargetAngle(newToLink.getToNode(), e.getValue().getFromNode());
                double linkDirection  = getTargetAngle(e.getValue().getToNode(), e.getValue().getFromNode());
                if (e.getValue().getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (e.getValue().getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        linkDirection = linkDirection;
                    }
                    else {
                        linkDirection += 3.06;
                    }
                }
                else {
                    if (e.getValue().getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        linkDirection = linkDirection;
                    } else {
                        linkDirection += 3.06;
                        linkDirection = linkDirection * -1;
                    }
                }
                if (newToLink.getToNode().getCoord().getY()>e.getValue().getFromNode().getCoord().getY()){
                    if (newToLink.getToNode().getCoord().getX()>e.getValue().getFromNode().getCoord().getX()){
                        targetRadiansNew = targetRadiansNew;
                    }
                    else {
                        targetRadiansNew += 3.06;
                    }
                }
                else {
                    if (newToLink.getToNode().getCoord().getX() > e.getValue().getFromNode().getCoord().getX()) {
                        targetRadiansNew = targetRadiansNew;
                    } else {
                        targetRadiansNew += 3.06;
                        targetRadiansNew = targetRadiansNew * -1;
                    }
                }
                double howfar = Math.abs(linkDirection - targetRadiansNew + random.nextDouble() * 1e-10);
                linkSamplerMin.add(howfar, e.getValue());


            });
            if (options.size() >= 10) {
                // the one with the smallest absolute angle relative to the current link will be taken as straight,
                //                if that angle is less than 45 degrees
                Double smallest = options.keySet().stream().reduce((x1, x2) -> {
                    if (x1 * x1 < x2 * x2)
                        return x1;
                    else
                        return x2;
                }).get();
                double euclideanDistanceFactor = getEuclideanDistanceFactor(euclideanDistanceFromOriginToDestination, euclideanDistanceToDestination);
                if (smallest < Math.PI / 4) {
                    Link straight = options.remove(smallest);
                    if (euclideanDistanceAttenuation) {
                        linkSampler.add(calcLinkUtil(ASC_Straight, getLinkWidth(straight), smallest - targetRadians, euclideanDistanceFactor), straight);
                    } else
                        linkSampler.add(calcLinkUtil(ASC_Straight, getLinkWidth(straight), smallest - targetRadians), straight);
                }
                // add the rest based on angle
                options.entrySet().forEach(e -> {
                    if (e.getKey() < smallest)
                        if (euclideanDistanceAttenuation)

                            linkSampler.add(calcLinkUtil(ASC_Left, getLinkWidth(e.getValue()), e.getKey() - targetRadians, euclideanDistanceFactor), e.getValue());
                        else
                            linkSampler.add(calcLinkUtil(ASC_Left, getLinkWidth(e.getValue()), e.getKey() - targetRadians), e.getValue());
                    else if (euclideanDistanceAttenuation)
                        linkSampler.add(calcLinkUtil(ASC_Right, getLinkWidth(e.getValue()), e.getKey() - targetRadians, euclideanDistanceFactor), e.getValue());
                    else
                        linkSampler.add(calcLinkUtil(ASC_Right, getLinkWidth(e.getValue()), e.getKey() - targetRadians), e.getValue());
                });

            } else if (options.size() == 1) {
                Link straight = options.firstEntry().getValue();
                linkSampler.add(calcLinkUtil(ASC_Straight, getLinkWidth(straight), 0d), straight);
            }
            Link choice = fromLink;
            // get a link
            if (options.size() >= 2) {
                double tempRan = new Random().nextDouble();
                if (tempRan > 0) {
                    choice = linkSamplerMin.selectRandom();
                } else {
                    choice = linkSampler.select();
                }
            } else {
                choice = linkSampler.select();
            }
           // cost += Math.log(linkSampler.getWeight(choice));
           // travelTime += choice.getLength(); //assume 1m/s for now
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
