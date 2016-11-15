package allow.adaptation.test;

import java.util.ArrayList;
import java.util.List;

import allow.simulator.adaptation.AdaptationManager;
import allow.simulator.adaptation.Ensemble;
import allow.simulator.adaptation.Issue;
import allow.simulator.adaptation.SelfishAdaptation;
import allow.simulator.entity.Gender;
import allow.simulator.entity.Person;
import allow.simulator.entity.Profile;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.mobility.planner.BikeRentalPlanner;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.OTPPlannerService;
import allow.simulator.mobility.planner.TaxiPlanner;
import allow.simulator.util.Coordinate;

public class AdaptationTest {

	public static void main(String[] args) {
		// Create JourneyPlanner instance - will be available through COntext during simulation
		OTPPlannerService otpPlanner = new OTPPlannerService("localhost", 8010);
		List<OTPPlannerService> otpPlanners = new ArrayList<OTPPlannerService>(1);
		otpPlanners.add(otpPlanner);
		TaxiPlanner taxiPlanner = new TaxiPlanner(otpPlanners, new Coordinate(11.1198448, 46.0719489));
		BikeRentalPlanner bikeRentalPlanner = new BikeRentalPlanner(otpPlanners, new Coordinate(11.1248895,46.0711398));
		JourneyPlanner planner = new JourneyPlanner(otpPlanners, taxiPlanner, bikeRentalPlanner, null);
		
		// EnsembleManager instance will be part of Context, too
		AdaptationManager ensembleManager = new AdaptationManager(new SelfishAdaptation(planner));
		
		// Create entities
		PublicTransportation bus = new PublicTransportation(1, null, null, 25);
		Person passenger1 = new Person(2, Gender.MALE, Profile.WORKER, null, null, new Coordinate(11.1318021, 46.0465206), true, true, true, null, null);
		passenger1.setCurrentItinerary(new Itinerary());
		passenger1.getCurrentItinerary().to = new Coordinate(11.1076075, 46.0487277);
		Person passenger2 = new Person(3, Gender.MALE, Profile.WORKER, null, null, new Coordinate(11.1318021, 46.0465206), true, true, true, null, null);
		passenger2.setCurrentItinerary(new Itinerary());
		passenger2.getCurrentItinerary().to = new Coordinate(11.1593422, 46.0875704);
		
		// Now, bus breakdown happens and bus driver creates ensemble
		bus.triggerIssue(Issue.BUS_BREAKDOWN);
		Ensemble ensemble = ensembleManager.createEnsemble(bus, "current-bus-trip-id-breakdown");
		
		// Passengers will add themselves once they realize the bus breakdown - happens locally at entities!
		Ensemble ensemble2 = ensembleManager.getEnsemble("current-bus-trip-id-breakdown");
		ensemble2.addEntity(passenger1);	
		Ensemble ensemble3 = ensembleManager.getEnsemble("current-bus-trip-id-breakdown");
		ensemble3.addEntity(passenger2);

		ensembleManager.runAdaptations();

		// After adaptation, creator needs to destroy the ensemble
		ensembleManager.terminateEnsemble("current-bus-trip-id-breakdown");
		
		try {
			planner.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
