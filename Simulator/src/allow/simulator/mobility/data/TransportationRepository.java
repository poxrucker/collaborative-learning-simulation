package allow.simulator.mobility.data;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import allow.simulator.core.Context;
import allow.simulator.entity.Bus;
import allow.simulator.entity.BusAgency;
import allow.simulator.entity.FlexiBusAgency;
import allow.simulator.entity.Taxi;
import allow.simulator.entity.TaxiAgency;
import allow.simulator.mobility.data.TimeTable.Day;
import allow.simulator.mobility.data.gtfs.GTFSAgency;
import allow.simulator.mobility.data.gtfs.GTFSRoute;
import allow.simulator.mobility.data.gtfs.GTFSService;
import allow.simulator.mobility.data.gtfs.GTFSServiceException;
import allow.simulator.mobility.data.gtfs.GTFSStop;
import allow.simulator.mobility.data.gtfs.GTFSStopTimes;
import allow.simulator.util.Coordinate;
import allow.simulator.world.Street;

public class TransportationRepository {
	// Collection of available GTFS transportation agencies.
	private Map<String, BusAgency> gtfsAgencies;

	// FlexiBus agency providing dynamic on-request bus scheduling.
	private FlexiBusAgency flexiBusAgency;
	
	// Taxi agency providing single and shared taxi rides
	private TaxiAgency taxiAgency;
	
	// Instance of object.
	private static TransportationRepository instance;
	
	private TransportationRepository(Context context) {
		// Load GTFS agencies and FlexiBus.
		reload(context);
	}
	
	private void reload(Context context) {
		// DataService
		IDataService dataService = context.getDataService();
		
		// Load public transportation from GTFS.
		List<GTFSAgency> agencyInfos = dataService.getAgencies();
		gtfsAgencies = new HashMap<String, BusAgency>();
		
		for (int i = 0; i < agencyInfos.size(); i++) {
			// Current agency.
			GTFSAgency currentAgency = agencyInfos.get(i);
			
			// Create new public transportation agency.
			BusAgency newAgency = new BusAgency(context.getEntityManager().getNextId(), context, currentAgency.getId());
			context.getEntityManager().addEntity(newAgency);

			// Request available routes.
			List<GTFSRoute> availableRoutes = dataService.getRoutes(currentAgency.getId());
		
			for (Iterator<GTFSRoute> it = availableRoutes.iterator(); it.hasNext(); ) {
				// Get Id of next route.				
				String routeId = it.next().getId();
				
				// Get time table of this route.
				TimeTable tt = dataService.getTimeTable(routeId);
				
				// Create stops of route from data service query.
				List<GTFSStop> stops = dataService.getStops(routeId);
				Map<String, BusStop> stopMap = new HashMap<String, BusStop>(stops.size());
				
				for (GTFSStop current : stops) {
					BusStop s = new BusStop(current.getName(), current.getId(), new Coordinate(current.getLon(), current.getLat()));
					stopMap.put(current.getId(), s);
				}
				
				// Create a new route for the agency and add it.
				Route newRoute = createRoute(routeId, stopMap, tt, dataService);
				newAgency.addRoute(newRoute);
				
				for (int k = 0; k < tt.getMaximalNumberOfTrips(); k++) {
					Bus b = new Bus(context.getEntityManager().getNextId(), context, newAgency, 25);
					context.getEntityManager().addEntity(b);
					newAgency.addPublicTransportation(b);
				}
			}
			gtfsAgencies.put(newAgency.getAgencyId(), newAgency);
		}
		
		// Create FlexiBus agency and assign buses.
		flexiBusAgency = new FlexiBusAgency(context.getEntityManager().getNextId(), context, "flexibusagency");
		context.getEntityManager().addEntity(flexiBusAgency);

		for (int i = 0; i < 200; i++) {
			// PublicTransportation b = (PublicTransportation) simulator.addEntity(Entity.Type.BUS);
			//b.setTransportationAgency(flexiBusAgency);
			//flexiBusAgency.addVehicle(b);
		}
		
		// Create taxi agency and assign taxis
		taxiAgency = new TaxiAgency(context.getEntityManager().getNextId(), context, "taxiagency");
		context.getEntityManager().addEntity(taxiAgency);

		for (int i = 0; i < 400; i++) {
			Taxi b = new Taxi(context.getEntityManager().getNextId(), context, taxiAgency, 3);
			context.getEntityManager().addEntity(b);
			taxiAgency.addTaxi(b);
		}
	}
	
	private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("kk:mm:ss");
	
	private Route createRoute(String routeId, Map<String, BusStop> stops, TimeTable tt, IDataService service) {
		// Allocate trips structure
		List<List<PublicTransportationTrip>> trips = new ArrayList<List<PublicTransportationTrip>>(7);
		Map<String, PublicTransportationTrip> tripInfo = new HashMap<String, PublicTransportationTrip>();
		
		for (int i = 0; i < 7; i++) {
			List<GTFSStopTimes> stoptimes = tt.getTripsOfDay(Day.values()[i]);
			LinkedList<PublicTransportationTrip> toAdd = new LinkedList<PublicTransportationTrip>();
			
			for (int j = 0; j < stoptimes.size(); j++) {
				// Current stop info.
				GTFSStopTimes info = stoptimes.get(j);

				// Allocate lists for times and stops and copy them.
				List<LocalTime> tripTimes = new ArrayList<LocalTime>(info.getStopIds().length);
				List<BusStop> tripStops = new ArrayList<BusStop>(info.getStopIds().length);
				
				for (int k = 0; k < info.getStopIds().length; k++) {
					tripStops.add(stops.get(info.getStopIds()[k]));
					tripTimes.add(LocalTime.parse(info.getDepartureTimes()[k], format));
				}
				
				// Allocate lists for traces and generate them.
				List<List<Street>> traces = new ArrayList<List<Street>>(tripStops.size() - 1);

				for (int l = 0; l < tripStops.size() - 1; l++) {
					BusStop curr = tripStops.get(l);
					BusStop next = tripStops.get(l + 1);
					List<Street> routing = service.getBusstopRouting(curr.getStopId(), next.getStopId());
					
					if (routing == null) routing = new ArrayList<Street>(0);
					traces.add(routing);
				}
				GTFSService serviceId = service.getServiceId(routeId, info.getTripId());
				List<GTFSServiceException> exceptions = service.getServiceExceptions(serviceId.getServiceId());
				PublicTransportationTrip t = new PublicTransportationTrip(info.getTripId(), serviceId.startDate(), serviceId.endDate(), exceptions, tripStops, tripTimes, traces);
				toAdd.addLast(t);
				
				if (!tripInfo.containsKey(t.getTripId())) tripInfo.put(t.getTripId(), t);
			}
			trips.add(toAdd);
		}
		return new Route(routeId, trips, tripInfo, stops);
	}
	
	public static TransportationRepository loadPublicTransportation(Context context) {
		instance = new TransportationRepository(context);
		return instance;
	}
	
	public static TransportationRepository Instance() {
		if (instance == null)
			throw new IllegalStateException("Error: Buslines not loaded. Execute loadBuslines() before.");
		
		return instance;
	}
	
	public BusAgency getGTFSTransportAgency(String agencyId) {
		return gtfsAgencies.get(agencyId);
	}
	
	public FlexiBusAgency getFlexiBusAgency() {
		return flexiBusAgency;
	}
	
	public TaxiAgency getTaxiAgency() {
		return taxiAgency;
	}
	
	public Map<String, BusAgency> getGTFSTransportAgencies() {
		return gtfsAgencies;
	}
}
