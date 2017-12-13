package allow.simulator.mobility.planner;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import scala.concurrent.forkjoin.ThreadLocalRandom;

public final class JourneyPlanner {
	// Threadpool execution service
	private ExecutorService service;
	
	// OTP planner instances
	private final List<OTPPlanner> otpPlanner;
	
	// Taxi planner service instance
	private final TaxiPlanner taxiPlanner;
	
	// Bike rental planner service instance
	private final BikeRentalPlanner bikeRentalPlanner;
	
	public JourneyPlanner(List<OTPPlanner> otpPlanner, TaxiPlanner taxiPlanner, 
			BikeRentalPlanner bikeRentalPlanner, ExecutorService service) {
		this.otpPlanner = otpPlanner;
		this.taxiPlanner = taxiPlanner;
		this.bikeRentalPlanner = bikeRentalPlanner;
		this.service = service;
	}
	
	public Future<List<Itinerary>> requestSingleJourney(List<JourneyRequest> requests, List<Itinerary> buffer) {
		return service.submit(new Callable<List<Itinerary>>() {

			@Override
			public List<Itinerary> call() throws Exception {	
				buffer.clear();
				
				for (JourneyRequest req : requests) {
					
					if (req.TransportTypes[0] == TType.SHARED_BICYCLE) {
						bikeRentalPlanner.requestSingleJourney(req, buffer);
						
					} else if ((req.TransportTypes[0] == TType.TAXI) || (req.TransportTypes[0] == TType.SHARED_TAXI)) {
						taxiPlanner.requestSingleJourney(req, buffer);
						
					} else {
						int i = 0;
						IPlannerService planner = otpPlanner.get(ThreadLocalRandom.current().nextInt(otpPlanner.size()));
						while (i < 2) {
							try {
								boolean success = planner.requestSingleJourney(req, buffer);

								if (success) {
									break;
								}
								i++;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}
				return buffer;
			}		
		});	
	}
	
	public TaxiPlanner getTaxiPlannerService() {
		return taxiPlanner;
	}
	
	public BikeRentalPlanner getBikeRentalPlannerService() {
		return bikeRentalPlanner;
	}	
}