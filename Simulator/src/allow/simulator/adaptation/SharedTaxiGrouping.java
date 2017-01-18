package allow.simulator.adaptation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.util.Coordinate;
import allow.simulator.util.Geometry;

public class SharedTaxiGrouping implements IGroupingAlgorithm {

	private double distThreshold;
	
	public SharedTaxiGrouping(double distThreshold) {
		this.distThreshold = distThreshold;
	}
	
	public Collection<Group> formGroups(Ensemble ensemble) {
		// Temporary buffer for group
		Map<Object, Group> finalGroups = new HashMap<Object, Group>();
		List<Group> groups = new ArrayList<Group>();

		// Retrieve the bus (creator) of the ensemble
		Entity creator = (Entity) ensemble.getLeader();
		Coordinate busPos = creator.getPosition();

		// Retrieve passengers already in the bus and add them as separate group 
		// to the temporary buffer
		List<IEnsembleParticipant> personsInBus = new ArrayList<IEnsembleParticipant>();
		List<IEnsembleParticipant> personsNotInBus = new ArrayList<IEnsembleParticipant>();
		
		for (IEnsembleParticipant p : ensemble.getEntities()) {
			
			if (!(p instanceof Person))
				continue;
			
			Coordinate cPos = ((Person)p).getPosition();
			
			if (distance(cPos.x, cPos.y, busPos.x, busPos.y, "K") == 0)
				personsInBus.add(p);
			else
				personsNotInBus.add(p);
		}
		
		int index = 1;
		
		if (personsInBus.size() > 0)
			finalGroups.put(index, new Group(personsInBus.get(0), personsInBus));

		
		for (IEnsembleParticipant p : ensemble.getEntities()) {
			
			if (!(p instanceof Person))
				continue;
			
			Person person = (Person)p;
			boolean assigned = tryAssign(person, groups);
			
			if (!assigned) {
				List<IEnsembleParticipant> participants = new ArrayList<IEnsembleParticipant>();
				participants.add(p);
				Group newGroup = new Group(p, participants);
				groups.add(newGroup);
			}
			
		}
		
		finalGroups = createGroups(personsInBus.size() > 0 ? personsInBus.get(0) : personsNotInBus.get(0), ensemble, finalGroups, personsNotInBus, index);
		Collection<Group> ret = finalGroups.values();
		return groups;
	}

	private boolean tryAssign(IEnsembleParticipant p, List<Group> groups) {
		
		for (Group g : groups) {
			
			if (g.getParticipants().size() < 4 &&
					Geometry.haversineDistance(((Entity)p).getPosition(), ((Entity)g.getLeader()).getPosition()) < distThreshold) {
				g.participants.add(p);
				return true;
			}
		}
		return false;
	}
	private static Map<Object, Group> createGroups(IEnsembleParticipant lastLeader,
			Ensemble ensemble, Map<Object, Group> finalGroups,
			List<IEnsembleParticipant> notAssigned, int index) {

		if (notAssigned == null) {
			// All entities assigned to a group, grouping has finished
			return finalGroups;
			
		} else {
			// Current position of the last leader
			Coordinate lastLeaderPosition = ((Entity)lastLeader).getPosition();

			// next leader calculation
			IEnsembleParticipant nextLeader = takeFirst(lastLeaderPosition, notAssigned, finalGroups);
			
			if (nextLeader != null) {
				// take position of the next leader
				Person leader = (allow.simulator.entity.Person) nextLeader;

				// retrieve passengers that are are <1Km from the next leader and are not in other groups
				Map<Object, List<IEnsembleParticipant>> result = ensemble
						.getEntities()
						.stream()
						.filter(p -> (NotBus(p) && NotInOtherGroups(p,
								finalGroups)))
						.collect(
								Collectors.groupingBy(p -> RightDistance(p,
										leader)));

				if (result.get(true) == null) {
					if (notAssigned.size() > 0) {
						// System.out.println("Passengers to assign");
						// for (int i = 0; i < notAssigned.size(); i++) {
						// System.out.println(notAssigned.get(i)
						// .getParticipantId());
						// }
					} else {
						System.out.println("All passengers assigned");
					}

				} else {
					// System.out.println("passengers to accomodate yet: "
					// + result.get(true));
					// create group with the people assigned
					List<IEnsembleParticipant> assigned = new ArrayList<IEnsembleParticipant>();
					for (Map.Entry<Object, List<IEnsembleParticipant>> entry : result
							.entrySet()) {
						boolean key = (boolean) entry.getKey();

						if (key) {
							assigned = entry.getValue();
						}
					}

					// System.out.println("assigned: " + assigned.toString());
					if (assigned != null) {
						Group group = new Group(nextLeader, assigned);
						index = index + 1;
						finalGroups.put(index, group);

						// take all passengers not yet assigned
						for (Map.Entry<Object, List<IEnsembleParticipant>> entry : result
								.entrySet()) {
							boolean key = (boolean) entry.getKey();
							if (!key) {
								// member not assigned yet
								notAssigned = entry.getValue();
							}
						}
						// System.out.println("not assigned: " +
						// notAssigned.toString());
						// recall the method recursively on not assigned
						if (notAssigned == null) {
							return finalGroups;
						} else if (notAssigned.size() == 1) {
							// group with only one participant
							Group group1 = new Group(nextLeader, notAssigned);
							index = index + 1;
							finalGroups.put(index, group1);

						} else {

							createGroups(nextLeader, ensemble, finalGroups,
									notAssigned, index);
						}
					} else {
						return finalGroups;
					}

				}

			}
		}
		return finalGroups;
	}
	
	private static double distance(double lat1, double lon1, double lat2, double lon2,
			String unit) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
				+ Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
				* Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		if (unit == "K") {
			dist = dist * 1.609344;
		} else if (unit == "N") {
			dist = dist * 0.8684;
		}

		return (dist);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts decimal degrees to radians : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	/* :: This function converts radians to decimal degrees : */
	/* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
	private static double rad2deg(double rad) {
		return (rad * 180 / Math.PI);
	}

	private static IEnsembleParticipant takeFirst(Coordinate busPos,
			List<IEnsembleParticipant> notAssigned,
			Map<Object, Group> finalGroups) {

		IEnsembleParticipant result = null;
		if (notAssigned.size() == 1) {
			result = notAssigned.get(0);
		} else {
			// search first the nearest one otherwise outside
			for (int i = 0; i < notAssigned.size(); i++) {
				IEnsembleParticipant current = notAssigned.get(i);
				if (current.getClass() == allow.simulator.entity.Person.class) {
					Coordinate cPos = ((Entity) current).getPosition();
					double distanceFromCreator = distance(cPos.y, cPos.x,
							busPos.y, busPos.x, "K");

					if (distanceFromCreator < 5
							&& NotInOtherGroups(current, finalGroups)) {
						// return the first "nearest" passenger
						result = current;
						break;
					}

				}
			}
			if (result == null) {
				result = notAssigned.get(0);
			}
		}
		return result;

	}

	private static boolean NotInOtherGroups(IEnsembleParticipant p, Map<Object, Group> otherGroups) {
		boolean result = true;
		
		for (Map.Entry<Object, Group> entry : otherGroups.entrySet()) {
			Group current = entry.getValue();
			List<IEnsembleParticipant> members = current.getParticipants();
			boolean find = false;
			
			for (int i = 0; i < members.size(); i++) {
				IEnsembleParticipant currentMember = members.get(i);
				
				if (currentMember.getParticipantId() == p.getParticipantId()) {
					find = true;
					break;
				}
			}
			
			if (find) {
				result = false;
				break;
			}

		}
		return result;
	}

	/*private static boolean RightDistanceFromBus(IEnsembleParticipant p, Coordinate busPos) {
		boolean result = false;
		Coordinate cPos = ((Entity) p).getPosition();
		double distanceFromCreator = distance(cPos.x, cPos.y, busPos.x, busPos.y, "K");
		
		if (distanceFromCreator < 3) {
			result = true;
		}
		return result;
	}*/

	private static boolean RightDistance(IEnsembleParticipant p, IEnsembleParticipant leader) {
		boolean result = true;
		Coordinate cPos = ((Entity) p).getPosition();
		Coordinate pPos = ((Entity) leader).getPosition();
		double distanceFromLeader = distance(cPos.x, cPos.y, pPos.x, pPos.y, "K");

		if (distanceFromLeader < 4.0)
			result = true;

		return result;
	}

	/*private static boolean NotInBus(IEnsembleParticipant p, Coordinate busPos) {
		boolean result = false;

		if (p.getClass() == allow.simulator.entity.Person.class) {

			Coordinate cPos = ((Entity) p).getPosition();
			double distanceFromCreator = distance(cPos.x, cPos.y, busPos.x,
					busPos.y, "K");
			if (distanceFromCreator == 0) {
				result = false;
			} else {
				result = true;
			}
		}

		return result;
	}*/

	private static boolean NotBus(IEnsembleParticipant p) {
		// TODO Auto-generated method stub
		boolean result = true;
		if (p.getClass() == allow.simulator.entity.Person.class) {
			result = true;
		} else {

			result = false;
		}
		return result;
	}
}
