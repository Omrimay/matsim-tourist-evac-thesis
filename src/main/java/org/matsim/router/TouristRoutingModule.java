package org.matsim.router;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.util.*;
import org.matsim.facilities.Facility;

import java.util.Arrays;
import java.util.List;

public class TouristRoutingModule implements RoutingModule {

    private final Network network;

    private final PopulationFactory populationFactory;
    TouristPathSimulatorLandmarkAndRandom pathSimulator = new TouristPathSimulatorLandmarkAndRandom();

    public TouristRoutingModule(Network network, PopulationFactory populationFactory) {
        this.network = network;
        this.populationFactory = populationFactory;
    }

    @Override
    public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime, Person person) {
        Leg newLeg = this.populationFactory.createLeg(TransportMode.car);

        Gbl.assertNotNull(fromFacility);
        Gbl.assertNotNull(toFacility);

        if (!toFacility.getLinkId().equals(fromFacility.getLinkId())) {
            // (a "true" route)
            Link fromLink = network.getLinks().get(fromFacility.getLinkId());
            Link toLink = network.getLinks().get(toFacility.getLinkId());

            LeastCostPathCalculator.Path path = pathSimulator.simulatePath(fromLink, toLink, departureTime, person, null);
            if (path == null) {
                throw new RuntimeException("No route found on inverted network from link "
                        + fromFacility.getLinkId() + " to link " + toFacility.getLinkId() + ".");
            }

            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromFacility.getLinkId(), toFacility.getLinkId());
            route.setLinkIds(fromFacility.getLinkId(), NetworkUtils.getLinkIds(path.links), toFacility.getLinkId());
            route.setTravelTime(path.travelTime);
            route.setTravelCost(path.travelCost);
            route.setDistance(RouteUtils.calcDistance(route, 1.0, 1.0, this.network));
            newLeg.setRoute(route);
            newLeg.setTravelTime(path.travelTime);
        } else {
            // create an empty route == staying on place if toLink == endLink
            // note that we still do a route: someone may drive from one location to another on the link. kai, dec'15
            NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromFacility.getLinkId(), toFacility.getLinkId());
            route.setTravelTime(0);
            route.setDistance(0.0);
            newLeg.setRoute(route);
            newLeg.setTravelTime(0);
        }
        newLeg.setDepartureTime(departureTime);
        return Arrays.asList( newLeg );

    }


}
