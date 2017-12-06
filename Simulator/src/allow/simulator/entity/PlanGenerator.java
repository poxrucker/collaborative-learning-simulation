package allow.simulator.entity;

import java.time.LocalTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

import allow.simulator.core.AllowSimulationModel;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.person.PlanJourney;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;
import allow.simulator.util.Pair;
import allow.simulator.world.StreetNode;
import allow.simulator.world.overlay.Area;
import allow.simulator.world.overlay.DistrictOverlay;
import allow.simulator.world.overlay.DistrictType;

public final class PlanGenerator {

	public static void generateDayPlan(Person person) {

		switch (person.getProfile()) {
		
		case CHILD:
			generateChildDayPlan(person);
			break;

		case STUDENT:
			generateStudentDayPlan(person);
			break;

		case WORKER:
			generateWorkerDayPlan(person);
			break;

		case HOMEMAKER:
			generateHomemakerDayPlan(person);
			break;

		default:
			throw new IllegalArgumentException("Error: Unknown person role "
					+ person.getProfile());

		}
	}

	private static Pair<LocalTime, Activity<Person>> createPlanJourney(Person p, TravelEvent t, long pOffsetSeconds) {
		LocalTime temp = t.getTime().plusSeconds(pOffsetSeconds).withSecond(0);
		return new Pair<LocalTime, Activity<Person>>(temp, new PlanJourney(p, t.getStartingPoint(), t.getDestination()));
	}
	
	private static void generateChildDayPlan(Person person) {
		int day = person.getContext().getTime().getCurrentDateTime()
				.getDayOfWeek().getValue();
		List<TravelEvent> routine = person.getDailyRoutine().getDailyRoutine(1);
		Queue<Pair<LocalTime, Activity<Person>>> schedule = person.getScheduleQueue();

		// Add daily routine travel events.
		for (TravelEvent t : routine) {
			long pOffsetSeconds = 0;
			
			if (t.arriveBy())
				pOffsetSeconds = -offsetFromDistance(t);
			else 
				pOffsetSeconds = ThreadLocalRandom.current().nextInt(10) * 60;
			schedule.add(createPlanJourney(person, t, pOffsetSeconds));
		}
	}

	private static int PROP_DEST_STUDENT[] = { 25, 5, 70, 0, 0, 0 };

	private static void generateStudentDayPlan(Person person) {
		DistrictOverlay partitioning = (DistrictOverlay) person.getContext().getWorld().getOverlay(AllowSimulationModel.OVERLAY_DISTRICTS);
		int day = person.getContext().getTime().getCurrentDateTime().getDayOfWeek().getValue();
		List<TravelEvent> routine = person.getDailyRoutine().getDailyRoutine(1);
		Queue<Pair<LocalTime, Activity<Person>>> schedule = person.getScheduleQueue();
		
		// 1. Home to work/university.
		TravelEvent homeToWork = routine.get(0);
		long pOffsetSeconds = -offsetFromDistance(homeToWork);
		schedule.add(createPlanJourney(person, homeToWork, pOffsetSeconds));

		// 2. From work back home. 
		TravelEvent workToHome = routine.get(1);

		if (workToHome.getHour() < 16) {
			int rand = ThreadLocalRandom.current().nextInt(100);
			
			if (rand < 10) {
				// Intermediate journey then home (home - work - destination - work - home).
				Coordinate dest = newLocation(partitioning, PROP_DEST_STUDENT);
				schedule.add(new Pair<LocalTime, Activity<Person>>(
								workToHome.getTime().plusSeconds(ThreadLocalRandom.current().nextInt(-5, 10) * 60), new PlanJourney(person,
										workToHome.getStartingPoint(), dest)));

				LocalTime fromDest = workToHome.getTime();
				schedule.add(new Pair<LocalTime, Activity<Person>>(fromDest,
								new PlanJourney(person, dest, homeToWork
										.getDestination())));

						schedule.add(new Pair<LocalTime, Activity<Person>>(fromDest,
								new PlanJourney(person, workToHome.getStartingPoint(),
										workToHome.getDestination())));

			} else if (rand < 20) {
				// Home then another journey afterwards (home - work - home - destination - home).
				schedule.add(new Pair<LocalTime, Activity<Person>>(
								workToHome.getTime().plusSeconds(ThreadLocalRandom.current().nextInt(-5, 10) * 60), new PlanJourney(person,
										workToHome.getStartingPoint(), workToHome
												.getDestination())));

						Coordinate dest = newLocation(partitioning, PROP_DEST_STUDENT);
						LocalTime toDest = workToHome.getTime();
						schedule.add(new Pair<LocalTime, Activity<Person>>(toDest,
								new PlanJourney(person, workToHome.getDestination(),
										dest)));

						schedule.add(new Pair<LocalTime, Activity<Person>>(toDest,
								new PlanJourney(person, dest, workToHome
										.getDestination())));

			} else if (rand < 50) {
				// Triangular (home - work - destination - home).
				Coordinate dest = newLocation(partitioning, PROP_DEST_STUDENT);
				schedule.add(new Pair<LocalTime, Activity<Person>>(
						workToHome.getTime().plusSeconds(ThreadLocalRandom.current().nextInt(-5, 10) * 60), new PlanJourney(person,
						workToHome.getStartingPoint(), dest)));

				schedule.add(new Pair<LocalTime, Activity<Person>>(
						workToHome.getTime(), new PlanJourney(person, dest,
						homeToWork.getStartingPoint())));
			} else {
				// Straight home (home - work - home).
				schedule.add(createPlanJourney(person, workToHome, ThreadLocalRandom.current().nextInt(10) * 60));
			}	
			
		} else {
			// Straight home (home - work - home).
			schedule.add(createPlanJourney(person, workToHome, ThreadLocalRandom.current().nextInt(10) * 60));
		}	
	}

	private static int PROP_DEST_WORKER[] = { 15, 10, 75, 0, 0, 0 };

	private static void generateWorkerDayPlan(Person person) {
		DistrictOverlay partitioning = (DistrictOverlay) person.getContext().getWorld().getOverlay(AllowSimulationModel.OVERLAY_DISTRICTS);
		int day = person.getContext().getTime().getCurrentDateTime().getDayOfWeek().getValue();
		List<TravelEvent> routine = person.getDailyRoutine().getDailyRoutine(1);
		Queue<Pair<LocalTime, Activity<Person>>> schedule = person.getScheduleQueue();

		// 1. Going to work in the morning.
		TravelEvent homeToWork = routine.get(0);
		long pOffsetSeconds = -offsetFromDistance(homeToWork);
		schedule.add(createPlanJourney(person, homeToWork, pOffsetSeconds));

		// 2. From work back home.
		TravelEvent workToHome = routine.get(1);
		int rand = ThreadLocalRandom.current().nextInt(100);
		
		if (rand < 10) {
			// Intermediate journey then home (home - work - destination - work - home).
			Coordinate dest = newLocation(partitioning, PROP_DEST_WORKER);
			schedule.add(new Pair<LocalTime, Activity<Person>>(workToHome.getTime(),
				new PlanJourney(person, workToHome.getStartingPoint(), dest)));

			LocalTime fromDest = workToHome.getTime();
			schedule.add(new Pair<LocalTime, Activity<Person>>(fromDest,
				new PlanJourney(person, dest, homeToWork.getDestination())));

			schedule.add(new Pair<LocalTime, Activity<Person>>(fromDest,
				new PlanJourney(person, workToHome.getStartingPoint(), workToHome.getDestination())));

		} else if (rand < 20) {
			// Home then another journey afterwards (home - work - home - destination - home).
			schedule.add(new Pair<LocalTime, Activity<Person>>(workToHome.getTime(),
				new PlanJourney(person, workToHome.getStartingPoint(), workToHome.getDestination())));

			Coordinate dest = newLocation(partitioning, PROP_DEST_WORKER);
			LocalTime toDest = workToHome.getTime();
			schedule.add(new Pair<LocalTime, Activity<Person>>(toDest,
					new PlanJourney(person, workToHome.getDestination(), dest)));

			schedule.add(new Pair<LocalTime, Activity<Person>>(toDest,
					new PlanJourney(person, dest, workToHome.getDestination())));

		} else if (rand < 45) {
			// Triangular (home - work - destination - home).
			Coordinate dest = newLocation(partitioning, PROP_DEST_WORKER);
			schedule.add(new Pair<LocalTime, Activity<Person>>(workToHome.getTime(),
					new PlanJourney(person, workToHome.getStartingPoint(), dest)));

			schedule.add(new Pair<LocalTime, Activity<Person>>(workToHome.getTime(),
					new PlanJourney(person, dest, homeToWork.getStartingPoint())));

		} else {
			// Straight home (home - work - home).
			schedule.add(createPlanJourney(person, workToHome, 0));
		}
	}

	private static int PROP_DEST_HOMEMAKER[] = { 25, 20, 25, 5, 0, 15 };

	private static void generateHomemakerDayPlan(Person person) {
		DistrictOverlay partitioning = (DistrictOverlay) person.getContext().getWorld().getOverlay(AllowSimulationModel.OVERLAY_DISTRICTS);
		Queue<Pair<LocalTime, Activity<Person>>> schedule = person.getScheduleQueue();

		// Journey in the morning?
		int rand = ThreadLocalRandom.current().nextInt(100);
		if (rand < 90) {
			// Random destination.
			Coordinate dest = newLocation(partitioning, PROP_DEST_HOMEMAKER);
			LocalTime tStart = gaussianPointInTime(600, 60);

			schedule.add(new Pair<LocalTime, Activity<Person>>(tStart, new PlanJourney(
					person, person.getHome(), dest)));

			schedule.add(new Pair<LocalTime, Activity<Person>>(
					tStart.plusMinutes(ThreadLocalRandom.current().nextInt(60,
							180)), new PlanJourney(person, dest, person
							.getHome())));
		}

		// Journey in the afternoon?
		rand = ThreadLocalRandom.current().nextInt(100);
		if (rand < 90) {
			// Random destination.
			Coordinate dest = newLocation(partitioning, PROP_DEST_HOMEMAKER);
			LocalTime tStart = gaussianPointInTime(960, 60);

			schedule.add(new Pair<LocalTime, Activity<Person>>(tStart, new PlanJourney(
					person, person.getHome(), dest)));

			schedule.add(new Pair<LocalTime, Activity<Person>>(
					tStart.plusMinutes(ThreadLocalRandom.current().nextInt(60,
							180)), new PlanJourney(person, dest, person
							.getHome())));
		}
	}

	private static long offsetFromDistance(TravelEvent t) {
		return (long) Geometry.haversineDistance(t.getStartingPoint(), t.getDestination()) / 4;
	}
	
	private static Coordinate newLocation(DistrictOverlay l, int distribution[]) {
		int r1 = ThreadLocalRandom.current().nextInt(100);
		DistrictType types[] = DistrictType.values();
		DistrictType t = types[0];
		int acc = 0;

		for (int i = 0; i < types.length; i++) {
			acc += distribution[i];

			if (r1 < acc) {
				t = types[i];
				break;
			}
		}
		List<Area> possibleAreas = l.getAreasOfType(t);
		Area a = possibleAreas.get(ThreadLocalRandom.current().nextInt(
				possibleAreas.size()));
		List<StreetNode> temp = l.getPointsInArea(a);
		return temp.get(ThreadLocalRandom.current().nextInt(temp.size()))
				.getPosition();
	}

	private static LocalTime gaussianPointInTime(double mean, double std) {
		int t = (int) ((ThreadLocalRandom.current().nextGaussian() * std) + mean);
		int hour = t / 60;
		return LocalTime.of(hour, t - hour * 60);
	}

}
