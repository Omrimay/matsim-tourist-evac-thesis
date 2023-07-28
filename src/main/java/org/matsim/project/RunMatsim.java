/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.project;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.router.util.TouristChoiceCoefficients;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.router.TouristRouting;

/**
 * @author nagel
 */
public class RunMatsim {

    public static void main(String[] args) {

        Config config;
        if (args == null || args.length == 0 || args[0] == null) {
            config = ConfigUtils.loadConfig("D:\\september 2022\\configTurMinNodesControl.xml");
        } else {
            //config = ConfigUtils.loadConfig(args);
            config = ConfigUtils.loadConfig("D:\\september 2022\\configTurMinNodesControl.xml");
        }

        config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

        // possibly modify config here

        // ---

        Scenario scenario = ScenarioUtils.loadScenario(config);

        // possibly modify scenario here

        // ---

        Controler controler = new Controler(scenario);


        // possibly modify controler here

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                TouristRouting routing = new TouristRouting(scenario.getPopulation().getFactory(), scenario.getNetwork());
                this.addRoutingModuleBinding(TransportMode.car).toProvider(routing);
            }
        });
        //controler.addOverridingModule(new OTFVisLiveModule());

        //turn landmark visibility in the routing on or off:
//        TouristChoiceCoefficients.landmarkVisibility = false;

        // ---

        controler.run();
    }

}
