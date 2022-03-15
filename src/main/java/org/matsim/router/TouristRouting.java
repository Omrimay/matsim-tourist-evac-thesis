package org.matsim.router;

import com.google.inject.Provider;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.RoutingModule;

import javax.inject.Inject;

public class TouristRouting implements Provider<RoutingModule> {

    @Override
    public RoutingModule get() {
        return new TouristRoutingModule();
    }
}
