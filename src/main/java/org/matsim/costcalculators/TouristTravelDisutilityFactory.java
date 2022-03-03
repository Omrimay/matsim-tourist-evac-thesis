package org.matsim.costcalculators;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;

public class TouristTravelDisutilityFactory implements TravelDisutilityFactory {

    @Override
    public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
        return new TouristTravelDisutility();
    }
}
