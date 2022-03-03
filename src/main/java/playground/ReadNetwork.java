package playground;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class ReadNetwork {
    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("test.xml");
        System.out.println();
    }
}
