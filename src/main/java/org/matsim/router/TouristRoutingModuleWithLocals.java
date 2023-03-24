package org.matsim.router;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.speedy.SpeedyALTFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TouristPathSimulatorLandmark;
import org.matsim.core.router.util.TouristPathSimulatortestRandom;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.Facility;
import org.matsim.vehicles.Vehicle;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TouristRoutingModuleWithLocals implements RoutingModule {

    private final Network network;

    private final PopulationFactory populationFactory;
    TouristPathSimulatorLandmark pathSimulatorLandmark = new TouristPathSimulatorLandmark();
    TouristPathSimulatortestRandom pathSimulatorRandom = new TouristPathSimulatortestRandom();


    public TouristRoutingModuleWithLocals(Network network, PopulationFactory populationFactory) {
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
            LeastCostPathCalculator.Path path = null;
            if (person.getAttributes().getAttribute("subpopulation").equals("tur")){
                path = pathSimulatorLandmark.simulatePath(fromLink, toLink, departureTime, person, null);

            } else {
                DistancePathCalculator alternativeShortestPath = new DistancePathCalculator(network);
                path = alternativeShortestPath.getPath(fromLink.getToNode(),toLink.getFromNode());
                NetworkRoute route = this.populationFactory.getRouteFactories().createRoute(NetworkRoute.class, fromFacility.getLinkId(), toFacility.getLinkId());
                route.setLinkIds(fromFacility.getLinkId(), NetworkUtils.getLinkIds(path.links), toFacility.getLinkId());
                route.setTravelTime(path.travelTime);
                route.setTravelCost(path.travelCost);
                route.setDistance(RouteUtils.calcDistance(route, 1.0, 1.0, this.network));
                newLeg.setRoute(route);
                newLeg.setTravelTime(path.travelTime);
            }
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
    private static class DistancePathCalculator {
        final static ThreadLocal<LeastCostPathCalculator> var = new ThreadLocal<>();
        final Network network;
        final LeastCostPathCalculator lcpc;

        // define how the travel disutility is computed:

        DistancePathCalculator(Network network) {
            this.network = network;

            // Cache this instance for each thread
            if (var.get() == null) {

                TravelDisutilityFactory disutilityFactory = new ConstantSpeedTravelDisutilityFactory();
                TravelTime travelTime = new ConstantSpeedTravelTime();

                var.set(new SpeedyALTFactory().createPathCalculator(network, disutilityFactory.createTravelDisutility(travelTime), travelTime));
            }

            lcpc = var.get();
        }

        LeastCostPathCalculator.Path getPath(Node fromNode, Node toNode) {

            return lcpc.calcLeastCostPath(fromNode, toNode, 0, null, null);
        }
    }

    private static class ConstantSpeedTravelTime implements TravelTime {
        @Override
        public double getLinkTravelTime(Link link, double v, Person person, Vehicle vehicle) {
            return link.getLength();
        }
    }

    private static class ConstantSpeedTravelDisutility implements TravelDisutility {
        @Override
        public double getLinkTravelDisutility(Link link, double v, Person person, Vehicle vehicle) {
            return link.getLength();
        }

        @Override
        public double getLinkMinimumTravelDisutility(Link link) {
            return link.getLength();
        }
    }

    private static class ConstantSpeedTravelDisutilityFactory implements TravelDisutilityFactory {
        @Override
        public TravelDisutility createTravelDisutility(TravelTime travelTime) {
            return new ConstantSpeedTravelDisutility();
        }
    }



}
