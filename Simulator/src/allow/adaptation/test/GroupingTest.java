package allow.adaptation.test;

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
import allow.simulator.mobility.planner.BikeRentalPlanner;
import allow.simulator.mobility.planner.Itinerary;
import allow.simulator.mobility.planner.JourneyPlanner;
import allow.simulator.mobility.planner.OTPPlannerService;
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
		bus.setPosition(new Coordinate(46.065194, 11.152867));
		Person passenger1 = new Person(2, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.065194, 11.152867), true, true, true,
				null, null);
		passenger1.setCurrentItinerary(new Itinerary());
		passenger1.getCurrentItinerary().to = new Coordinate(11.1076075,
				46.0487277);
		Person passenger2 = new Person(3, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.065194, 11.152867), true, true, true,
				null, null);
		passenger2.setCurrentItinerary(new Itinerary());
		passenger2.getCurrentItinerary().to = new Coordinate(11.1593422,
				46.0875704);
		Person passenger3 = new Person(4, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.065194, 11.152867), true, true, true,
				null, null);
		passenger3.setCurrentItinerary(new Itinerary());
		passenger3.getCurrentItinerary().to = new Coordinate(11.1593422,
				46.0875704);

		Person passenger4 = new Person(5, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.065194, 11.152867), true, true, true,
				null, null);
		passenger4.setCurrentItinerary(new Itinerary());
		passenger4.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger5 = new Person(6, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.067859, 11.151397), true, true, true,
				null, null);
		passenger5.setCurrentItinerary(new Itinerary());
		passenger5.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger6 = new Person(7, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.067859, 11.151397), true, true, true,
				null, null);
		passenger6.setCurrentItinerary(new Itinerary());
		passenger6.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger7 = new Person(8, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.067859, 11.151397), true, true, true,
				null, null);
		passenger7.setCurrentItinerary(new Itinerary());
		passenger7.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger8 = new Person(9, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.067859, 11.151397), true, true, true,
				null, null);
		passenger8.setCurrentItinerary(new Itinerary());
		passenger8.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger9 = new Person(10, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(46.067859, 11.151397), true, true, true,
				null, null);
		passenger9.setCurrentItinerary(new Itinerary());
		passenger9.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger10 = new Person(11, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(56.067859, 11.171397), true, true, true,
				null, null);
		passenger10.setCurrentItinerary(new Itinerary());
		passenger10.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger11 = new Person(12, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(66.067859, 11.171397), true, true, true,
				null, null);
		passenger11.setCurrentItinerary(new Itinerary());
		passenger11.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger12 = new Person(13, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(66.067859, 11.171397), true, true, true,
				null, null);
		passenger12.setCurrentItinerary(new Itinerary());
		passenger12.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

		Person passenger13 = new Person(14, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(66.067859, 11.171397), true, true, true,
				null, null);
		passenger13.setCurrentItinerary(new Itinerary());
		passenger13.getCurrentItinerary().to = new Coordinate(46.066221,
				11.154240);

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
		ensemble.addEntity(passenger10);
		ensemble.addEntity(passenger11);
		ensemble.addEntity(passenger12);
		ensemble.addEntity(passenger13);
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

		// Group newGroup =
		ensembleManager.CreateGroups(creator, ensemble, finalGroups,
				notAssigned, index);

		// System.out.println("FINAL GROUPS: " + finalGroups.toString());
		// System.out.println("Gruppo 1: "
		// + finalGroups.get(1).getParticipants().toString());
		// System.out.println("Gruppo 2: "
		// + finalGroups.get(2).getParticipants().toString());

	}
}
