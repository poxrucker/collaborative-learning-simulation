package allow.simulator.adaptation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import allow.simulator.entity.Entity;
import allow.simulator.entity.Person;
import allow.simulator.entity.PublicTransportation;
import allow.simulator.util.Coordinate;

public final class AdaptationManager {
	// Collection of all ensembles registered to this EnsembleManager instance
	private final Map<String, Ensemble> ensembles;

	// Adaptation strategy to run
	private final IAdaptationStrategy adaptationStrategy;

	/**
	 * Creates a new instance of an AdaptationManager.
	 */
	public AdaptationManager(IAdaptationStrategy adaptationStrategy) {
		ensembles = new HashMap<String, Ensemble>();
		this.adaptationStrategy = adaptationStrategy;
	}

	/**
	 * Creates a new ensemble with given Id and creator and adds it to the
	 * collection of ensembles maintained within the AdaptationManager.
	 * 
	 * @param creator
	 *            Entity which is the creator of the ensemble
	 * @param ensembleId
	 *            Id of the ensemble
	 * @return New ensemble instance
	 */
	public Ensemble createEnsemble(IEnsembleParticipant creator,
			String ensembleId) {
		if (ensembles.containsKey(ensembleId))
			throw new IllegalStateException("Error: Ensemble " + ensembleId
					+ " already exists.");

		Ensemble ensemble = new Ensemble(creator);
		ensembles.put(ensembleId, ensemble);
		return ensemble;
	}

	/**
	 * Removes the ensemble with the given Id from this instance.
	 * 
	 * @param ensembleId
	 *            Id of ensemble to remove
	 * @return True if ensemble was removed, false if no ensemble with the given
	 *         Id is registered in this AdaptationManager instance
	 */
	public boolean terminateEnsemble(String ensembleId) {
		return (ensembles.remove(ensembleId) != null);
	}

	/**
	 * Returns the ensemble with the given Id.
	 * 
	 * @param ensembleId
	 *            Id of ensemble to return
	 * @return Ensemble with given Id or null, if no ensemble with the given Id
	 *         is registered in this AdaptationManager instance
	 */
	public Ensemble getEnsemble(String ensembleId) {
		return ensembles.get(ensembleId);
	}

	public void runAdaptations() {
		final Collection<Ensemble> temp = ensembles.values();

		for (Ensemble ensemble : temp) {
			Issue issue = ensemble.getCreator().getTriggeredIssue();

			if (issue != Issue.NONE)
				adaptationStrategy.solveAdaptation(issue, ensemble);
		}
	}

	public void groupPassengers(Ensemble e) {

	}

	public double distance(double lat1, double lon1, double lat2, double lon2,
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

	public IEnsembleParticipant takeFirst(Coordinate busPos,
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
					double distanceFromCreator = distance(cPos.x, cPos.y,
							busPos.x, busPos.y, "K");

					if (distanceFromCreator < 1
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

	public boolean NotInOtherGroups(IEnsembleParticipant p,
			Map<Object, Group> otherGroups) {
		boolean result = false;
		for (Map.Entry<Object, Group> entry : otherGroups.entrySet()) {
			Group current = entry.getValue();
			List<IEnsembleParticipant> members = current.getParticipants();
			if (!members.contains(p)) {
				result = true;
				break;
			} else {
				result = false;
			}

		}

		// TODO Auto-generated method stub
		return result;
	}

	public boolean RightDistance(IEnsembleParticipant p, Coordinate busPos) {
		boolean result = false;
		Coordinate cPos = ((Entity) p).getPosition();
		double distanceFromCreator = distance(cPos.x, cPos.y, busPos.x,
				busPos.y, "K");
		if (distanceFromCreator < 2) {
			result = true;
		}
		return result;
	}

	public boolean NotInBus(IEnsembleParticipant p, Coordinate busPos) {
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
	}

	public void CreateGroups(IEnsembleParticipant lastLeader,
			Ensemble ensemble, Map<Object, Group> finalGroups,
			List<IEnsembleParticipant> notAssigned, int index) {

		if (notAssigned == null) {
			// all entities assigned to a group
			return;
		} else {
			// current position of the last leader
			Coordinate lastLeaderPosition = null;
			if (lastLeader.getClass() == allow.simulator.entity.Person.class) {
				lastLeaderPosition = ((Entity) lastLeader).getPosition();
			} else if (lastLeader.getClass() == allow.simulator.entity.PublicTransportation.class) {
				lastLeaderPosition = ((PublicTransportation) lastLeader)
						.getPosition();
			}
			// next leader calculation
			IEnsembleParticipant nextLeader = takeFirst(lastLeaderPosition,
					notAssigned, finalGroups);
			if (nextLeader != null) {

				// take position of the next leader

				Person leader = (allow.simulator.entity.Person) nextLeader;
				Coordinate lPos = leader.getPosition();

				// retrieve passengers thare are <1Km from the next leader and
				// are not in other groups

				Map<Object, List<IEnsembleParticipant>> result = ensemble
						.getEntities()
						.stream()
						.filter(p -> (NotInBus(p, lPos) && NotInOtherGroups(p,
								finalGroups)))
						.collect(
								Collectors.groupingBy(p -> RightDistance(p,
										lPos)));

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
					if (notAssigned.size() == 1) {
						// group with only one participant
						Group group1 = new Group(nextLeader, notAssigned);
						index = index + 1;
						finalGroups.put(index, group1);

					} else {
						CreateGroups(nextLeader, ensemble, finalGroups,
								notAssigned, index);
					}
				} else {
					return;
				}

			}

		}

	}
}