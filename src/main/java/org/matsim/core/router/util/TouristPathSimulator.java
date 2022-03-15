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

public class TouristPathSimulator {
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

    double calcLinkUtil(double ASC, double width) {
        return Math.exp(ASC + widthCoeficient * width + random.nextDouble() * 1e-10);
    }

    public LeastCostPathCalculator.Path simulatePath(Link fromLink, Link toLink, double starttime, Person person, Vehicle vehicle) {
        AbstractSequentialList<Node> nodes = new LinkedList<>();
        AbstractSequentialList<Link> links = new LinkedList<>();

        nodes.add(fromLink.getFromNode());
        links.add(fromLink);
        Link currentLink = fromLink;
        double cost = 0d;
        double travelTime = 0d;
        while (!currentLink.equals(toLink)) {
            RandomCollection<Link> linkSampler = new RandomCollection<>();
            Link back = NetworkUtils.findLinkInOppositeDirection(currentLink);
            if (back != null) {
                linkSampler.add(calcLinkUtil(ASC_Back, getLinkWidth(back)), back);
            }
            TreeMap<Double, Link> options = NetworkUtils.getOutLinksSortedClockwiseByAngle(currentLink);
            if (options.size() >= 2) {
                // the one with the smallest absolute angle relative to the current link will be taken as straight,
                //                if that angle is less than 45 degrees
                Double smallest = options.keySet().stream().reduce((x1, x2) -> {
                    if (x1 * x1 < x2 * x2)
                        return x1;
                    else
                        return x2;
                }).get();
                if (smallest < Math.PI / 4) {
                    Link straight = options.remove(smallest);
                    linkSampler.add(calcLinkUtil(ASC_Straight, getLinkWidth(straight)), straight);
                }
                // add the rest based on angle
                options.entrySet().forEach(e -> {
                    if (e.getKey() < smallest)
                        linkSampler.add(calcLinkUtil(ASC_Left, getLinkWidth(e.getValue())), e.getValue());
                    else
                        linkSampler.add(calcLinkUtil(ASC_Right, getLinkWidth(e.getValue())), e.getValue());
                });

            } else if (options.size() == 1) {
                Link straight = options.firstEntry().getValue();
                linkSampler.add(calcLinkUtil(ASC_Straight, getLinkWidth(straight)), straight);
            }

            // get a link
            Link choice = linkSampler.select();
            cost += Math.log(linkSampler.getWeight(choice));
            travelTime += choice.getLength(); //assume 1m/s for now
            links.add(choice);
            nodes.add(choice.getFromNode());
            currentLink = choice;

        }
        nodes.add(currentLink.getToNode());
        links.remove(0);
        links.remove(links.size()-1);


        return new LeastCostPathCalculator.Path(nodes, links, travelTime, cost);
    }
}
