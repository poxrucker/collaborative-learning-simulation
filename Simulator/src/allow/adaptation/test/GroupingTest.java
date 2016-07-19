package allow.adaptation.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import allow.simulator.adaptation.AdaptationManager;
import allow.simulator.adaptation.Ensemble;
import allow.simulator.adaptation.Group;
import allow.simulator.adaptation.IEnsembleParticipant;
import allow.simulator.adaptation.SelfishAdaptation;
import allow.simulator.entity.Gender;
import allow.simulator.entity.Person;
import allow.simulator.entity.Profile;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.mobility.data.TType;
import allow.simulator.mobility.planner.BikeRentalPlanner;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.JourneyRequest;
import allow.simulator.mobility.planner.OTPPlannerService;
import allow.simulator.mobility.planner.RequestId;
import allow.simulator.mobility.planner.TaxiPlanner;
import allow.simulator.util.Coordinate;

public class GroupingTest {

	public static void main(String[] args) {
		// Create JourneyPlanner instance - will be available through COntext
		// during simulation
		OTPPlannerService otpPlanner = new OTPPlannerService("localhost", 8010);
		List<OTPPlannerService> otpPlanners = new ArrayList<OTPPlannerService>(
				1);
		otpPlanners.add(otpPlanner);
		TaxiPlanner taxiPlanner = new TaxiPlanner(otpPlanners, new Coordinate(
				11.1198448, 46.0719489));
		BikeRentalPlanner bikeRentalPlanner = new BikeRentalPlanner(
				otpPlanners, new Coordinate(11.1248895, 46.0711398));
		JourneyPlanner planner = new JourneyPlanner(otpPlanners, taxiPlanner,
				bikeRentalPlanner, null);

		// EnsembleManager instance will be part of Context, too
		AdaptationManager ensembleManager = new AdaptationManager(
				new SelfishAdaptation(planner));

		// Create entities
		PublicTransportation bus = new PublicTransportation(1, null, null,
				null, null, 25);
		bus.setPosition(new Coordinate(11.152867, 46.065194));
		Person passenger1 = new Person(2, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.152867, 46.065194), true, true, true,
				null, null);
		passenger1.setCurrentItinerary(new Itinerary());
		passenger1.getCurrentItinerary().to = new Coordinate(11.1076075,
				46.0487277);
		Person passenger2 = new Person(3, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.152867, 46.065194), true, true, true,
				null, null);
		passenger2.setCurrentItinerary(new Itinerary());
		passenger2.getCurrentItinerary().to = new Coordinate(11.1593422,
				46.0875704);
		Person passenger3 = new Person(4, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.152867, 46.065194), true, true, true,
				null, null);
		passenger3.setCurrentItinerary(new Itinerary());
		passenger3.getCurrentItinerary().to = new Coordinate(11.1593422,
				46.0875704);

		Person passenger4 = new Person(5, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.152867, 46.065194), true, true, true,
				null, null);
		passenger4.setCurrentItinerary(new Itinerary());
		passenger4.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		Person passenger5 = new Person(6, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.151397, 46.067859), true, true, true,
				null, null);
		passenger5.setCurrentItinerary(new Itinerary());
		passenger5.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		Person passenger6 = new Person(7, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.151397, 46.067859), true, true, true,
				null, null);
		passenger6.setCurrentItinerary(new Itinerary());
		passenger6.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		Person passenger7 = new Person(8, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.151397, 46.067859), true, true, true,
				null, null);
		passenger7.setCurrentItinerary(new Itinerary());
		passenger7.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		Person passenger8 = new Person(9, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.151397, 46.067859), true, true, true,
				null, null);
		passenger8.setCurrentItinerary(new Itinerary());
		passenger8.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		Person passenger9 = new Person(10, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.151397, 46.067859), true, true, true,
				null, null);
		passenger9.setCurrentItinerary(new Itinerary());
		passenger9.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		// Create Ensemble
		Ensemble ensemble = ensembleManager.createEnsemble(bus,
				"current-bus-trip-id-breakdown");
		ensemble.addEntity(passenger1);
		ensemble.addEntity(passenger2);
		ensemble.addEntity(passenger3);
		ensemble.addEntity(passenger4);
		ensemble.addEntity(passenger5);
		ensemble.addEntity(passenger6);
		ensemble.addEntity(passenger7);
		ensemble.addEntity(passenger8);
		ensemble.addEntity(passenger9);

		ensemble.addEntity(bus);

		// FINAL MAP OF GROUPS

		Map<Object, Group> finalGroups = new HashMap<Object, Group>();

		// Retrieve the bus (creator) of the ensemble
		PublicTransportation creator = (PublicTransportation) ensemble
				.getCreator();

		Coordinate busPos = creator.getPosition();

		// retrieve passengers already in the bus

		List<IEnsembleParticipant> personInBus = new ArrayList<IEnsembleParticipant>();
		ensemble.getEntities()
				.forEach(
						(temp) -> {
							if (temp.getClass() == allow.simulator.entity.Person.class) {
								Person p = (allow.simulator.entity.Person) temp;
								Coordinate cPos = p.getPosition();
								double distanceFromCreator = ensembleManager
										.distance(cPos.x, cPos.y, busPos.x,
												busPos.y, "K");
								if (distanceFromCreator == 0) {
									personInBus.add(p);
								}
							}

						});

		Group InBus = new Group(creator, personInBus);

		// retrieve entities not in the bus
		List<IEnsembleParticipant> notAssigned = new ArrayList<IEnsembleParticipant>();

		ensemble.getEntities()
				.forEach(
						(temp) -> {
							if (!personInBus.contains(temp)
									&& (temp.getClass() == allow.simulator.entity.Person.class)) {
								notAssigned.add(temp);
							}

						});

		int index = 1;
		// add groups of person already in the bus at the final result of groups
		finalGroups.put(index, InBus);

		Map<Object, Group> Groups = ensembleManager.CreateGroups(creator,
				ensemble, finalGroups, notAssigned, index);

		System.out.println(" ######## FINAL GROUPS ######## "
				+ finalGroups.size());

		for (int i = 1; i <= finalGroups.size(); i++) {
			System.out.println("Group " + i + ": "
					+ finalGroups.get(i).getParticipants().toString());
			System.out.println("- Leader: "
					+ finalGroups.get(i).getLeader().toString());

		}

		// take a group as an example
		Group g = finalGroups.get(1);
		PublicTransportation leader = (allow.simulator.entity.PublicTransportation) g
				.getLeader();
		// Coordinate
		// Person leader = (allow.simulator.entity.Person) g.getLeader();
		Coordinate from = leader.getPosition();
		;

		List<Coordinate> startingPoints = new ArrayList<Coordinate>();
		List<Coordinate> destinations = new ArrayList<Coordinate>();
		
		for (int i = 0; i < g.getParticipants().size(); i++) {
			Person p = (allow.simulator.entity.Person) g.getParticipants().get(i);
			startingPoints.add(p.getPosition());
			destinations.add(p.getCurrentItinerary().to);
		}

		RequestId reqId = new RequestId();

		TType[] mean = new TType[1];
		mean[0] = TType.SHARED_TAXI;
		boolean arriveBy = false;

		String str = "2016-07-12 12:30";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

		// For each group derive the journey for each participant
		JourneyRequest r = JourneyRequest.createRequest(from, startingPoints,
				destinations, dateTime, arriveBy, mean, reqId);

		// Planning Instantiation
		OTPPlannerService otp = new OTPPlannerService("localhost", 8010);
		List<OTPPlannerService> planners = new ArrayList<OTPPlannerService>();
		planners.add(otp);

		// TaxiPlanner Instantiation
		Coordinate taxiRank = new Coordinate(11.1198448, 46.0719489);
		TaxiPlanner tp = new TaxiPlanner(planners, taxiRank);

		// request taxi journey
		List<Itinerary> resultItineraries = new ArrayList<Itinerary>();

		tp.requestSingleJourney(r, resultItineraries);
		System.out
				.println("Number of Itineraries: " + resultItineraries.size());

	}
}
