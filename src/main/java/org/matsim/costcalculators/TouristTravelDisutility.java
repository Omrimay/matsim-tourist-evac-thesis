package org.matsim.costcalculators;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;

public class TouristTravelDisutility implements TravelDisutility {
    @Override
    public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
        double width = (double) link.getAttributes().getAttribute("width");

        return 0;
    }

    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return 0;
    }
}
