package allow.simulator.mobility.planner;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import scala.concurrent.forkjoin.ThreadLocalRandom;
import allow.simulator.mobility.data.TType;

public final class JourneyPlanner {
	// Threadpool execution service
	private ExecutorService service;
	
	// OTP planner instances
	private final List<OTPPlanner> otpPlanner;
	
	// Taxi planner service instance
	private final TaxiPlanner taxiPlanner;
	
	// FlexiBus planner service instance
	private final FlexiBusPlanner flexiBusPlanner;
	
	// Bike rental planner service instance
	private final BikeRentalPlanner bikeRentalPlanner;
	
	public JourneyPlanner(List<OTPPlanner> otpPlanner, TaxiPlanner taxiPlanner, 
			BikeRentalPlanner bikeRentalPlanner, FlexiBusPlanner flexiBusPlanner) {
		this.otpPlanner = otpPlanner;
		this.taxiPlanner = taxiPlanner;
		this.bikeRentalPlanner = bikeRentalPlanner;
		this.flexiBusPlanner = flexiBusPlanner;
	}
	
	public Future<List<Itinerary>> requestSingleJourney(List<JourneyRequest> requests, List<Itinerary> buffer) {
		if (service == null)
			initialize();
		return service.submit(new Callable<List<Itinerary>>() {

			@Override
			public List<Itinerary> call() throws Exception {	
				buffer.clear();
				
				for (JourneyRequest req : requests) {
					
					if (req.TransportTypes[0] == TType.FLEXIBUS) {
						flexiBusPlanner.requestSingleJourney(req, buffer);
					
					} else if (req.TransportTypes[0] == TType.SHARED_BICYCLE) {
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
	
	public FlexiBusPlanner getFlexiBusPlannerService() {
		return flexiBusPlanner;
	}
	
	public BikeRentalPlanner getBikeRentalPlannerService() {
		return bikeRentalPlanner;
	}
	
	public void shutdown() throws InterruptedException {
		service.shutdown();
		service.awaitTermination(30, TimeUnit.SECONDS);
	}
	
	private void initialize() {
		service = Executors.newFixedThreadPool(32);
	}
}
