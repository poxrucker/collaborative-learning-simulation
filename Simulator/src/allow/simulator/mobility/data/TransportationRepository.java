package allow.simulator.mobility.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import allow.simulator.core.Context;
import allow.simulator.entity.FlexiBusAgency;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.entity.PublicTransportationAgency;
import allow.simulator.entity.Taxi;
import allow.simulator.entity.TaxiAgency;
import allow.simulator.mobility.data.gtfs.GTFSAgency;
import allow.simulator.mobility.data.gtfs.GTFSRoute;
import allow.simulator.mobility.data.gtfs.GTFSStop;
import allow.simulator.util.Coordinate;

public class TransportationRepository {
	// Collection of available GTFS transportation agencies.
	private Map<String, PublicTransportationAgency> gtfsAgencies;

	// FlexiBus agency providing dynamic on-request bus scheduling.
	private FlexiBusAgency flexiBusAgency;
	
	// Taxi agency providing single and shared taxi rides
	private TaxiAgency taxiAgency;
	
	// Instance of object.
	private static TransportationRepository instance;
	
	private TransportationRepository(Context simulator) {
		// Load GTFS agencies and FlexiBus.
		reload(simulator);
	}
	
	private void reload(Context context) {
		// Load public transportation from GTFS.
		List<GTFSAgency> agencyInfos = context.getDataService().getAgencies();
		gtfsAgencies = new HashMap<String, PublicTransportationAgency>();
		
		for (int i = 0; i < agencyInfos.size(); i++) {
			// Current agency.
			GTFSAgency currentAgency = agencyInfos.get(i);
			
			// Create new public transportation agency.
			PublicTransportationAgency newAgency = new PublicTransportationAgency(context.getEntityManager().getNextId(), context, currentAgency.getId());
			context.getEntityManager().addEntity(newAgency);

			// Request available routes.
			List<GTFSRoute> availableRoutes = context.getDataService().getRoutes(currentAgency.getId());
		
			for (Iterator<GTFSRoute> it = availableRoutes.iterator(); it.hasNext(); ) {
				// Get Id of next route.				
				String routeId = it.next().getId();
				
				// Get time table of this route.
				TimeTable tt = context.getDataService().getTimeTable(routeId);
				
				// Create stops of route from data service query.
				List<GTFSStop> stops = context.getDataService().getStops(routeId);
				Map<String, PublicTransportationStop> stopMap = new HashMap<String, PublicTransportationStop>(stops.size());
				
				for (GTFSStop current : stops) {
					PublicTransportationStop s = new PublicTransportationStop(current.getName(), current.getId(), new Coordinate(current.getLon(), current.getLat()));
					stopMap.put(current.getId(), s);
				}
				
				// Create a new route for the agency and add it.
				Route newRoute = new Route(routeId, tt, stopMap);
				newAgency.addRoute(newRoute);
				
				for (int k = 0; k < tt.getMaximalNumberOfTrips(); k++) {
					PublicTransportation b = new PublicTransportation(context.getEntityManager().getNextId(), context, newAgency, 25);
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
	
	public static TransportationRepository loadPublicTransportation(Context context) {
		instance = new TransportationRepository(context);
		return instance;
	}
	
	public static TransportationRepository Instance() {
		if (instance == null)
			throw new IllegalStateException("Error: Buslines not loaded. Execute loadBuslines() before.");
		
		return instance;
	}
	
	public PublicTransportationAgency getGTFSTransportAgency(String agencyId) {
		return gtfsAgencies.get(agencyId);
	}
	
	public FlexiBusAgency getFlexiBusAgency() {
		return flexiBusAgency;
	}
	
	public TaxiAgency getTaxiAgency() {
		return taxiAgency;
	}
	
	public Map<String, PublicTransportationAgency> getGTFSTransportAgencies() {
		return gtfsAgencies;
	}
}
