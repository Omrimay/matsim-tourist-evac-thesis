package playground;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.router.costcalculators.RandomizingTimeDistanceTravelDisutilityFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.costcalculators.TouristTravelDisutilityFactory;

public class MyOwnControler {
    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig() ;
        config.controler().setOverwriteFileSetting( OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists );
        config.controler().setLastIteration(1);

        config.plansCalcRoute().setRoutingRandomness( 3. );
        // (This is currently the default anyways. kai, mar'20)

        Scenario scenario = ScenarioUtils.createScenario(config) ;

        Controler controler = new Controler( scenario ) ;

        controler.addOverridingModule(new AbstractModule(){
            @Override public void install() {
                addTravelDisutilityFactoryBinding( TransportMode.walk ).toInstance(
                        new TouristTravelDisutilityFactory( ) );
                // (This is currently the default anyways. kai, mar'20)
            }
        });

        // this sets the routing randomness (currently between time and money only, so be careful
        // that you have a monetary term in the standard disutility, e.g. a distance cost)


        controler.run();
    }
}
