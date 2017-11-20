package allow.simulator.statistics;

import java.util.Collection;

import allow.simulator.core.Context;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.utility.Preferences;
import allow.simulator.world.StreetSegment;

public class Statistics {
	// Prior and posterior travel times for car journeys
	private SlidingWindow priorCarTravelTime;
	private SlidingWindow posteriorCarTravelTime;
	
	// Prior and posterior travel times for bus journeys
	private SlidingWindow priorBusTravelTime;
	private SlidingWindow posteriorBusTravelTime;
	
	// Prior and posterior travel times for bike journeys
	private SlidingWindow priorBikeTravelTime;
	private SlidingWindow posteriorBikeTravelTime;
	
	// Prior and posterior travel times for walking journeys
	private SlidingWindow priorWalkTravelTime;
	private SlidingWindow posteriorWalkTravelTime;
	
	// Experienced bus filling level
	private SlidingWindow busFillingLevel;
	
	// Prior and posterior utility for car journeys
	private SlidingWindow priorUtilityCar;
	private SlidingWindow posteriorUtilityCar;
	
	// Prior and posterior utility for bus journeys
	private SlidingWindow priorUtilityBus;
	private SlidingWindow posteriorUtilityBus;
	
	// Waiting time when replaning 
	private SlidingWindow replaningWaitingTime;
	
	private double meanBusPreference;
	private double meanCarPreference;
	
	private long numberOfCarJourneys;
	private long numberOfTransitJourneys;
	private long numberOfBikeJourneys;
	private long numberOfWalkJourneys;
	private long numberOfTaxiJourneys;
	private double carJourneysRatio;
	private double transitJourneysRatio;
	private double bikeJourneysRatio;
	private double walkJourneysRatio;
	private double taxiJourneyRatio;
	
	// Number of times an informed and affected entity used the alternative planner
	// before starting its trip
	private int informedPlannings;
	private int informedPlanningsAffected;

	// Number of times an entity arrives at the construction site and needs to replan
	// using the alternative planner
	private int constructionSiteReplannings;
	
	// Number of times an entity gets informed about the construction site during its trip
	// and replans using the alternative planner
	private int intermediateReplannings;
	
	// Total number of times entities planned journeys
	private int totalNumberOfPlannings;
	private int totalNumberOfAffectedPlannings;
	
	private SlidingWindow priorCarTravelTimeConstructionSiteRaw;
	private SlidingWindow posteriorCarTravelTimeConstructionSiteRaw;

	private SlidingWindow priorCarTravelTimeConstructionSiteActual;
	private SlidingWindow posteriorCarTravelTimeConstructionSiteActual;

	private SlidingWindow priorTripDistance;
	private SlidingWindow posteriorTripDistance;

	private CoverageStatistics coverageStats;
	
	private int successfulParking;
	private int failedParking;
	private int reasonMaxSearchTimeExceeded;
	private int reasonNoPath;
	private SlidingWindow meanParkingSpotSearchTime;
	private SlidingWindow meanParkingSpotSearchUtility;
	private SlidingWindow meanParkingSpotCost;
	private SlidingWindow meanParkingSpotWalkingDistance;
	
	public Statistics(int windowSize) {
		priorCarTravelTime = new SlidingWindow(windowSize);
		posteriorCarTravelTime = new SlidingWindow(windowSize);
		
		priorBusTravelTime = new SlidingWindow(windowSize);
		posteriorBusTravelTime = new SlidingWindow(windowSize);
		
		priorBikeTravelTime = new SlidingWindow(windowSize);
		posteriorBikeTravelTime = new SlidingWindow(windowSize);
		
		priorWalkTravelTime = new SlidingWindow(windowSize);
		posteriorWalkTravelTime = new SlidingWindow(windowSize);
		
		busFillingLevel = new SlidingWindow(windowSize);
		
		priorUtilityCar = new SlidingWindow(windowSize);
		posteriorUtilityCar = new SlidingWindow(windowSize);
		
		priorUtilityBus = new SlidingWindow(windowSize);
		posteriorUtilityBus = new SlidingWindow(windowSize);
		
		replaningWaitingTime = new SlidingWindow(windowSize);
		
		meanBusPreference = 0.5;
		meanCarPreference = 0.5;
		numberOfCarJourneys = 0;
		numberOfTransitJourneys = 0;
		numberOfBikeJourneys = 0;
		numberOfWalkJourneys = 0;
		numberOfTaxiJourneys = 0;
		
		informedPlannings = 0;
		informedPlanningsAffected = 0;
		intermediateReplannings = 0;
		constructionSiteReplannings = 0;
		totalNumberOfPlannings = 0;
		totalNumberOfAffectedPlannings = 0;
		
		priorCarTravelTimeConstructionSiteRaw = new SlidingWindow(windowSize);
		posteriorCarTravelTimeConstructionSiteRaw = new SlidingWindow(windowSize);
		
		priorCarTravelTimeConstructionSiteActual = new SlidingWindow(windowSize);
		posteriorCarTravelTimeConstructionSiteActual = new SlidingWindow(windowSize);
		
		priorTripDistance = new SlidingWindow(windowSize);
		posteriorTripDistance = new SlidingWindow(windowSize);
		
		meanParkingSpotSearchTime = new SlidingWindow(windowSize);
		meanParkingSpotSearchUtility = new SlidingWindow(windowSize);
		meanParkingSpotCost = new SlidingWindow(windowSize);
		meanParkingSpotWalkingDistance = new SlidingWindow(windowSize);
	}
	
	public void reset() {
		priorCarTravelTime.reset();
		posteriorCarTravelTime.reset();
		
		priorBusTravelTime.reset();
		posteriorBusTravelTime.reset();
		
		priorBikeTravelTime.reset();
		posteriorBikeTravelTime.reset();
		
		priorWalkTravelTime.reset();
		posteriorWalkTravelTime.reset();
		
		busFillingLevel.reset();
		
		priorUtilityCar.reset();
		posteriorUtilityCar.reset();
		
		priorUtilityBus.reset();
		posteriorUtilityBus.reset();
		
		priorCarTravelTimeConstructionSiteRaw.reset();
		posteriorCarTravelTimeConstructionSiteRaw.reset();
		
		priorCarTravelTimeConstructionSiteActual.reset();
		posteriorCarTravelTimeConstructionSiteActual.reset();
		
		priorTripDistance.reset();
		posteriorTripDistance.reset();
		
		informedPlannings = 0;
		informedPlanningsAffected = 0;
		intermediateReplannings = 0;
		constructionSiteReplannings = 0;
		totalNumberOfPlannings = 0;
		totalNumberOfAffectedPlannings = 0;
		
		meanParkingSpotSearchTime.reset();
		meanParkingSpotSearchUtility.reset();
		meanParkingSpotCost.reset();
		meanParkingSpotWalkingDistance.reset();
		successfulParking = 0;
		failedParking = 0;
		reasonMaxSearchTimeExceeded = 0;
		reasonNoPath = 0;
	}
	
	public double getCarJourneyRatio() {
		return carJourneysRatio;
	}
	
	public double getTransitJourneyRatio() {
		return transitJourneysRatio;
	}
	
	public double getBikeJourneyRatio() {
		return bikeJourneysRatio;
	}
	
	public double getWalkJourneyRatio() {
		return walkJourneysRatio;
	}
	
	public double getTaxiJourneyRatio() {
		return taxiJourneyRatio;
	}
	
	public double getMeanPriorCarTravelTime() {
		return priorCarTravelTime.getMean();
	}
	
	public double getMeanPosteriorCarTravelTime() {
		return posteriorCarTravelTime.getMean();
	}
	
	public double getMeanPriorCarTravelTimeConstructionSiteRaw() {
		return priorCarTravelTimeConstructionSiteRaw.getMean();
	}
	
	public double getMeanPosteriorCarTravelTimeConstructionSiteRaw() {
		return posteriorCarTravelTimeConstructionSiteRaw.getMean();
	}
	
	public double getMeanPriorCarTravelTimeConstructionSiteActual() {
		return priorCarTravelTimeConstructionSiteActual.getMean();
	}
	
	public double getMeanPosteriorCarTravelTimeConstructionSiteActual() {
		return posteriorCarTravelTimeConstructionSiteActual.getMean();
	}
	
	public double getMeanPriorTripDistance() {
		return priorTripDistance.getMean();
	}
	
	public double getMeanPosteriorPriorTripDistance() {
		return posteriorTripDistance.getMean();
	}
	
	public double getMeanPriorBusTravelTime() {
		return priorBusTravelTime.getMean();
	}
	
	public double getMeanPosteriorBusTravelTime() {
		return posteriorBusTravelTime.getMean();
	}
	
	public double getMeanBusPreference() {
		return meanBusPreference;
	}
	
	public double getMeanCarPreference() {
		return meanCarPreference;
	}
	
	public double getMeanBusFillingLevel() {
		return busFillingLevel.getMean();
	}
	
	public double getMeanPriorUtilityCar() {
		return priorUtilityCar.getMean();
	}
	
	public double getMeanPosteriorUtilityCar() {
		return posteriorUtilityCar.getMean();
	}
	
	public double getMeanPriorUtilityBus() {
		return priorUtilityBus.getMean();
	}
	
	public double getMeanPosteriorUtilityBus() {
		return posteriorUtilityBus.getMean();
	}
	
	public double getMeanReplaningWaitingTime() {
		return replaningWaitingTime.getMean();
	}
	
	public int getNumberOfPlannings() {
		return totalNumberOfPlannings;
	}
	
	public int getNumberOfAffectedPlannings() {
		return totalNumberOfAffectedPlannings;
	}
	
	public int getInformedPlannings() {
		return informedPlannings;
	}
	
	public int getInformedPlanningsAffected() {
		return informedPlanningsAffected;
	}
	
	public int getIntermediateReplannings() {
		return intermediateReplannings;
	}
	
	public int getConstructionSiteReplannings() {
		return constructionSiteReplannings;
	}
	
	public void reportPlanning() {
		totalNumberOfPlannings++;
	}
	
	public void reportInformedPlanning() {
		informedPlannings++;
		totalNumberOfPlannings++;
	}
	
	public void reportPlanningAffected() {
		totalNumberOfAffectedPlannings++;
	}
	
	public void reportInformedPlanningAffected() {
		informedPlanningsAffected++;
		totalNumberOfAffectedPlannings++;
	}
	
	public void reportIntermediateReplanning() {
		intermediateReplannings++;
	}
	
	public void reportConstructionSiteReplanning() {
		constructionSiteReplannings++;
	}

	public void reportCarJourney() {
		numberOfCarJourneys++;
	}
	
	public void reportTransitJourney() {
		numberOfTransitJourneys++;
	}
	
	public void reportBikeJourney() {
		numberOfBikeJourneys++;
	}
	
	public void reportWalkJourney() {
		numberOfWalkJourneys++;
	}
	
	public void reportTaxiJourney() {
		numberOfTaxiJourneys++;
	}
	
	public void reportPriorAndPosteriorCarTravelTimesConstructionSiteRaw(double priorToAdd, double posteriorToAdd) {
		priorCarTravelTimeConstructionSiteRaw.addValue(priorToAdd);
		posteriorCarTravelTimeConstructionSiteRaw.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorCarTravelTimesConstructionSiteActual(double priorToAdd, double posteriorToAdd) {
		priorCarTravelTimeConstructionSiteActual.addValue(priorToAdd);
		posteriorCarTravelTimeConstructionSiteActual.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorTripDistance(double priorToAdd, double posteriorToAdd) {
		priorTripDistance.addValue(priorToAdd);
		posteriorTripDistance.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorCarTravelTimes(double priorToAdd, double posteriorToAdd) {
		priorCarTravelTime.addValue(priorToAdd);
		posteriorCarTravelTime.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorTransitTravelTimes(double priorToAdd, double posteriorToAdd) {
		priorBusTravelTime.addValue(priorToAdd);
		posteriorBusTravelTime.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorBikeTravelTimes(double priorToAdd, double posteriorToAdd) {
		priorBikeTravelTime.addValue(priorToAdd);
		posteriorBikeTravelTime.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorWalkTravelTimes(double priorToAdd, double posteriorToAdd) {
		priorWalkTravelTime.addValue(priorToAdd);
		posteriorWalkTravelTime.addValue(posteriorToAdd);
	}
	
	public void reportBusFillingLevel(double fillingLevel) {
		busFillingLevel.addValue(fillingLevel);
	}
	
	public void reportReplaningWaitingTime(double waitingTime) {
		replaningWaitingTime.addValue(waitingTime);
	}
	
	public void reportPriorAndPosteriorUtilityCar(double priorToAdd, double posteriorToAdd) {
		priorUtilityCar.addValue(priorToAdd);
		posteriorUtilityCar.addValue(posteriorToAdd);
	}
	
	public void reportPriorAndPosteriorUtilityBus(double priorToAdd, double posteriorToAdd) {
		priorUtilityBus.addValue(priorToAdd);
		posteriorUtilityBus.addValue(posteriorToAdd);
	}
	
	public double getTotalStreetNetworkLength() {
	  return coverageStats.getTotalNetworkLength();
	}
	
	public double getVisitedStreetNetworkLength() {
	  return coverageStats.getVisitedNetworkLength();
	}
	
	public void setCoverageStats(CoverageStatistics stats) {
	  this.coverageStats = stats;
	}
	
	public void reportVisitedLink(long time, StreetSegment seg) {
	  coverageStats.updateSegment(time, seg);
	}
	
	public void reportSuccessfulParking() {
	  successfulParking++;
	}
	
	public int getSuccessfulParking() {
	  return successfulParking;
	}
	
	public void reportFailedParking(int reason) {
	  failedParking++;
	  
	  if (reason == 0)
	    reasonMaxSearchTimeExceeded++;
	  
	  else if (reason == 1)
	    reasonNoPath++;
	}
	
	public int getFailedParking() {
	  return failedParking;
	}
	
	public int getReasonMaxSearchTimeExceeded() {
	  return reasonMaxSearchTimeExceeded;
	}
	
	public int getReasonNoPath() {
	  return reasonNoPath;
	}
	
	public void reportSearchTimeParking(double searchTime) {
	  meanParkingSpotSearchTime.addValue(searchTime);
	}
	
	public double getMeanSearchTimeParking() {
	  return meanParkingSpotSearchTime.getMean();
	}
	
	public void reportParkingCosts(double costs) {
    meanParkingSpotCost.addValue(costs);
  }
  
  public double getMeanParkingCosts() {
    return meanParkingSpotCost.getMean();
  }
  
  public void reportParkingWalkingDistance(double walkingDistance) {
    meanParkingSpotWalkingDistance.addValue(walkingDistance);
  }
  
  public double getMeanParkingWalkingDistance() {
    return meanParkingSpotWalkingDistance.getMean();
  }
	
	public void reportSearchUtility(double utility) {
    meanParkingSpotSearchUtility.addValue(utility);
  }
  
  public double getMeanUtilityParking() {
    return meanParkingSpotSearchUtility.getMean();
  }
	
	public void updateGlobalStatistics(Context simulationContext) {
		Collection<Entity> persons = simulationContext.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
		updateMeanTransportPreferences(persons);
		updateJourneyChoices();
		coverageStats.updateStatistics(simulationContext.getTime().getTimestamp());
	}
	
	private void updateMeanTransportPreferences(Collection<Entity> persons) {
		// int busPrefAcc = 0;
		// int carPrefAcc = 0;
		double busPrefAcc = 0.0;
		double carPrefAcc = 0.0;
		
		for (Entity entity : persons) {
			Person person = (Person) entity;
			Preferences p = person.getRankingFunction().getPreferences();
			
			/*if (p.getBusPreference() >= p.getCarPreference()) {
				busPrefAcc++;
			} else {
				carPrefAcc++;
			}*/
			busPrefAcc += p.getBusPreference();
			carPrefAcc += p.getCarPreference();
		}
		meanBusPreference = busPrefAcc / persons.size();
		meanCarPreference = carPrefAcc / persons.size();
	}
	
	private void updateJourneyChoices() {
		double totalNumberOfJourneys = numberOfCarJourneys + numberOfTransitJourneys 
				+ numberOfBikeJourneys + numberOfWalkJourneys + numberOfTaxiJourneys;
		
		if (totalNumberOfJourneys == 0) return;
		
		carJourneysRatio = (double) numberOfCarJourneys / totalNumberOfJourneys;
		transitJourneysRatio = (double) numberOfTransitJourneys / totalNumberOfJourneys;
		bikeJourneysRatio = (double) numberOfBikeJourneys / totalNumberOfJourneys;
		walkJourneysRatio = (double) numberOfWalkJourneys / totalNumberOfJourneys;
		taxiJourneyRatio = (double) numberOfTaxiJourneys / totalNumberOfJourneys;	
	}
}
