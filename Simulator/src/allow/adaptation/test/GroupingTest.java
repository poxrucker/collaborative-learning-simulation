package allow.adaptation.test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

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
		bus.setPosition(new Coordinate(11.161968, 46.072994));

		// P1: from povo to central station
		Person passenger1 = new Person(2, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.161968, 46.072994), true, true, true,
				null, null);
		passenger1.setCurrentItinerary(new Itinerary());
		passenger1.getCurrentItinerary().to = new Coordinate(11.120432,
				46.072294);

		// P2: from povo to central station
		Person passenger2 = new Person(3, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.161968, 46.072994), true, true, true,
				null, null);

		passenger2.setCurrentItinerary(new Itinerary());

		passenger2.getCurrentItinerary().to = new Coordinate(11.120432,
				46.072294);

		// from Mesiano to Central Station
		Person passenger3 = new Person(4, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger3.setCurrentItinerary(new Itinerary());
		passenger3.getCurrentItinerary().to = new Coordinate(11.120432,
				46.072294);

		// from Mesiano to Central Station
		Person passenger4 = new Person(5, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger4.setCurrentItinerary(new Itinerary());
		passenger4.getCurrentItinerary().to = new Coordinate(11.120432,
				46.072294);

		// from Mesiano to Central Station
		Person passenger5 = new Person(6, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger5.setCurrentItinerary(new Itinerary());
		passenger5.getCurrentItinerary().to = new Coordinate(11.120432,
				46.072294);

		// from Mesiano to Central Station
		Person passenger6 = new Person(7, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger6.setCurrentItinerary(new Itinerary());
		passenger6.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		// from Mesiano to Central Station
		Person passenger7 = new Person(8, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger7.setCurrentItinerary(new Itinerary());
		passenger7.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		// from Mesiano to Central Station
		Person passenger8 = new Person(9, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
				null, null);
		passenger8.setCurrentItinerary(new Itinerary());
		passenger8.getCurrentItinerary().to = new Coordinate(11.154240,
				46.066221);

		// from Mesiano to Central Station
		Person passenger9 = new Person(10, Gender.MALE, Profile.WORKER, null,
				null, new Coordinate(11.142697, 46.065027), true, true, true,
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
		Group g = finalGroups.get(2);
		// PublicTransportation leader =
		// (allow.simulator.entity.PublicTransportation) g
		// .getLeader();
		// Coordinate
		Person leader = (allow.simulator.entity.Person) g.getLeader();
		Coordinate from = leader.getPosition();

		List<Coordinate> startingPoints = new ArrayList<Coordinate>();
		List<Coordinate> destinations = new ArrayList<Coordinate>();

		for (int i = 0; i < g.getParticipants().size(); i++) {
			Person p = (allow.simulator.entity.Person) g.getParticipants().get(
					i);
			startingPoints.add(p.getPosition());
			destinations.add(p.getCurrentItinerary().to);
		}

		RequestId reqId = new RequestId();

		TType[] mean = new TType[1];
		mean[0] = TType.SHARED_TAXI;
		boolean arriveBy = false;

		String str = "2016-08-25 12:30";
		DateTimeFormatter formatter = DateTimeFormatter
				.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

		// For each group derive the journey for each participant
		JourneyRequest r = JourneyRequest.createSharedRequest(from,
				startingPoints, destinations, dateTime, arriveBy, mean, reqId);

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
		System.out.println("Trip Type : "
				+ resultItineraries.get(0).itineraryType);
		for (int i = 0; i < resultItineraries.size(); i++) {
			System.out.println("Itinerary  : "
					+ resultItineraries.get(i).toString());

		}
		for (int i = 0; i < resultItineraries.get(0).subItineraries.size(); i++) {
			System.out
					.println("Sub Itinerary  : "
							+ resultItineraries.get(0).subItineraries.get(i)
									.toString());
			System.out
					.println("Walking Distance  : "
							+ resultItineraries.get(0).subItineraries.get(i).walkDistance);
			System.out.println("Walking Time  : "
					+ resultItineraries.get(0).subItineraries.get(i).walkTime);
		}

		// print the result using a map
		ShowOnMapNew(resultItineraries);

	}

	private static void ShowOnMapNew(List<Itinerary> itineraries) {
		JXMapViewer mapViewer = new JXMapViewer();

		// Create a TileFactoryInfo for OpenStreetMap
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);
		mapViewer.setTileFactory(tileFactory);

		// Set the focus on Trento
		GeoPosition trento = new GeoPosition(46.0719489, 11.1198448);
		mapViewer.setZoom(7);
		mapViewer.setAddressLocation(trento);

		GeoPosition[] positions = new GeoPosition[0];
		DefaultWaypoint[] waypoints = new DefaultWaypoint[0];

		for (int i = 0; i < itineraries.get(0).subItineraries.size(); i++) {
			// System.out.println(i);
			// System.out.println("worker: "
			// + itineraries.get(0).subItineraries.get(i).reqId);
			GeoPosition position = new GeoPosition(
					itineraries.get(0).subItineraries.get(i).from.y,
					itineraries.get(0).subItineraries.get(i).from.x);
			// System.out.println("position: " + position);
			positions = addElement(positions, position);
			DefaultWaypoint point = new DefaultWaypoint(position);
			waypoints = addPoint(waypoints, point);

		}

		List<GeoPosition> track = Arrays.asList(positions);
		RoutePainter routePainter = new RoutePainter(track);

		// Create waypoints from the geo-positions
		Set<Waypoint> waypointsSet = new HashSet<Waypoint>(
				Arrays.asList(waypoints));

		// Create a waypoint painter that takes all the waypoints
		WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(waypointsSet);

		// Create a compound painter that uses both the route-painter and the
		// waypoint-painter
		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePainter);
		painters.add(waypointPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(
				painters);
		mapViewer.setOverlayPainter(painter);

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("Collective Adaptation");
		frame.getContentPane().add(mapViewer);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	private static GeoPosition[] addElement(GeoPosition[] positions,
			GeoPosition position) {
		GeoPosition[] result = Arrays.copyOf(positions, positions.length + 1);
		result[positions.length] = position;
		return result;
	}

	private static DefaultWaypoint[] addPoint(DefaultWaypoint[] points,
			DefaultWaypoint point) {
		DefaultWaypoint[] result = Arrays.copyOf(points, points.length + 1);
		result[points.length] = point;
		return result;
	}
}
