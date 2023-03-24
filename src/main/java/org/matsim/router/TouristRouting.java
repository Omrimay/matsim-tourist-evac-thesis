package org.matsim.router;

import com.google.inject.Provider;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.router.RoutingModule;

public class TouristRouting implements Provider<RoutingModule> {

    private final PopulationFactory populationFactory;
    private final Network network;

    public TouristRouting(PopulationFactory populationFactory, Network network) {
        this.populationFactory = populationFactory;
        this.network = network;
    }

    @Override
    public RoutingModule get() {
        return new TouristRoutingModuleWithLocals(this.network, this.populationFactory);
    }
}
