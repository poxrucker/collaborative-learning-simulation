package allow.simulator.mobility.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import allow.simulator.mobility.data.gtfs.GTFSAgency;
import allow.simulator.mobility.data.gtfs.GTFSRoute;
import allow.simulator.mobility.data.gtfs.GTFSService;
import allow.simulator.mobility.data.gtfs.GTFSServiceException;
import allow.simulator.mobility.data.gtfs.GTFSStop;
import allow.simulator.world.Street;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OnlineDataService implements IDataService {

	// Client to send requests.
	private HttpClient client;

	// Host requests are send to.
	private HttpHost target;
	private HttpGet getRequest;

	private static String routeURI = "/otp/routers/default/index/routes";
	private static String agencyURI = "/otp/routers/default/index/agencies";
	private static String stopURI = "/otp/routers/default/index/routes/%s/stops";

	private static ObjectMapper mapper = new ObjectMapper();

	public OnlineDataService(String host, int port) {
		target = new HttpHost(host, port, "http");
		client = new DefaultHttpClient();
		getRequest = new HttpGet();
	}

	@Override
	public List<GTFSRoute> getRoutes(String agencyId) {

		if (agencyId == null) {
			throw new IllegalArgumentException("Incomplete request parameters");
		}
		// Create http request and execute it.
		try {
			getRequest.setURI(new URI(routeURI));
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());

			// Parse answer here.
			return mapper.readValue(res, new TypeReference<List<GTFSRoute>>() {
			});

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRoutesRaw(String agencyId) {
		
		if (agencyId == null) {
			throw new IllegalArgumentException("Incomplete request parameters");
		}
		// Create http request and execute it.
		try {
			getRequest.setURI(new URI(routeURI));
			
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());
			return res;

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<GTFSStop> getStops(String routeId) {

		if (routeId == null) {
			throw new IllegalArgumentException("Incomplete request parameters");
		}
	
		try {
			getRequest.setURI(new URI(String.format(stopURI, routeId)));
			
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());
			System.out.println(res);
			// Parse answer here.
			return mapper.readValue(res, new TypeReference<List<GTFSStop>>() {
			});

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getStopsRaw(String routeId) {

		if (routeId == null) {
			throw new IllegalArgumentException("Incomplete request parameters");
		}
		
		try {
			getRequest.setURI(new URI(String.format(stopURI, routeId)));
					
			} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());
			return res;

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public TimeTable getTimeTable(String routeId) {

		if (routeId == null) {
			throw new IllegalArgumentException("Incomplete request parameters");
		}
		
		// Get stops of route.
		// List<Stop> stops = getStops(routeId);
		
		// Get stop times of route.
		
		return null;
	}

	@Override
	public List<GTFSAgency> getAgencies() {
		// Create http request and execute it.
		try {
			getRequest.setURI(new URI(agencyURI));
							
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			HttpResponse httpResponse = client.execute(target, getRequest);
			String res = EntityUtils.toString(httpResponse.getEntity());

			// Parse answer here.
			return mapper.readValue(res, new TypeReference<List<GTFSAgency>>() {
			});

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<GTFSServiceException> getServiceExceptions(String serviceId) {
		return new ArrayList<GTFSServiceException>(0);
	}

	@Override
	public GTFSService getServiceId(String routeId, String tripId) {
		return null;
	}

	@Override
	public List<Street> getBusstopRouting(String start, String end) {
		return null;
	}
}
