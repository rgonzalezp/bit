package tracker;

import utils.Datafile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;

/**
 * The client's interface to the Tracker.
 */
public class TrackerClient {

    private InetSocketAddress client, server;
    private Datafile datafile;

    public TrackerClient(InetSocketAddress client, InetSocketAddress server, Datafile datafile) {
        this.client = client;
        this.server = server;
        this.datafile = datafile;
    }

    public TrackerResponse update(TrackerRequest.Event event) throws IOException {
    	
    	InetAddress ip = InetAddress.getLocalHost();
        
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets))
        {
        	if( netint.getName().toString().equals("ens160"))
            {
        		 Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                 for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                 	ip = inetAddress;
                 }
            }
             
        }
        try (Socket socket = new Socket(ip, server.getPort())) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            TrackerRequest req = new TrackerRequest(event, client, datafile.getFilename());
            req.send(out);
            return TrackerResponse.fromStream(in);
        }
    }

    public InetSocketAddress getClient() {
        return client;
    }

    public InetSocketAddress getServer() {
        return server;
    }

    public Datafile getDatafile() {
        return datafile;
    }
}