package allow.simulator.statistics;

import java.util.Collection;

import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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

  // Number of times an informed and affected entity used the alternative
  // planner
  // before starting its trip
  private int informedPlannings;
  private int informedPlanningsAffected;

  // Number of times an entity arrives at the construction site and needs to
  // replan
  // using the alternative planner
  private int constructionSiteReplannings;

  // Number of times an entity gets informed about the construction site during
  // its trip
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

  // Identifier for different journey types
  private static final int CAR = 0;
  private static final int BUS = 1;
  private static final int WALK = 2;
  private static final int BIKE = 3;
  private static final int TAXI = 4;

  // Identifier for parking success and failure
  private static final int SUCCESS = 0;
  private static final int FAILURE = 1;

  // Counts journey type frequencies
  private Frequency journeyTypeFrequencies;

  // Counts parking success/failures
  private Frequency parkingSuccess;

  // Counts different reasons why parking failed
  private Frequency parkingFailureReason;

  private DescriptiveStatistics parkingSpotSearchUtility;
  private DescriptiveStatistics parkingSpotSearchTime;
  private DescriptiveStatistics parkingSpotCost;
  private DescriptiveStatistics parkingSpotWalkingDistance;

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

    journeyTypeFrequencies = new Frequency();
    parkingSuccess = new Frequency();
    parkingFailureReason = new Frequency();
    parkingSpotSearchTime = new DescriptiveStatistics(new double[windowSize]);
    parkingSpotSearchTime.setWindowSize(windowSize);
    parkingSpotSearchUtility = new DescriptiveStatistics(new double[windowSize]);
    parkingSpotSearchUtility.setWindowSize(windowSize);
    parkingSpotCost = new DescriptiveStatistics(new double[windowSize]);
    parkingSpotCost.setWindowSize(windowSize);
    parkingSpotWalkingDistance = new DescriptiveStatistics(new double[windowSize]);
    parkingSpotWalkingDistance.setWindowSize(windowSize);
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

    journeyTypeFrequencies.clear();
    parkingSuccess.clear();
    parkingFailureReason.clear();
    parkingSpotSearchTime.clear();
    parkingSpotSearchUtility.clear();
    parkingSpotCost.clear();
    parkingSpotWalkingDistance.clear();
  }

  public double getCarJourneyRatio() {
    long sum = journeyTypeFrequencies.getSumFreq();
    return (sum > 0) ? (double) journeyTypeFrequencies.getCount(CAR) / (double) sum : 0.0;
  }

  public double getTransitJourneyRatio() {
    long sum = journeyTypeFrequencies.getSumFreq();
    return (sum > 0) ? (double) journeyTypeFrequencies.getCount(BUS) / (double) sum : 0.0;
  }

  public double getBikeJourneyRatio() {
    long sum = journeyTypeFrequencies.getSumFreq();
    return (sum > 0) ? (double) journeyTypeFrequencies.getCount(BIKE) / (double) sum : 0.0;
  }

  public double getWalkJourneyRatio() {
    long sum = journeyTypeFrequencies.getSumFreq();
    return (sum > 0) ? (double) journeyTypeFrequencies.getCount(WALK) / (double) sum : 0.0;
  }

  public double getTaxiJourneyRatio() {
    long sum = journeyTypeFrequencies.getSumFreq();
    return (sum > 0) ? (double) journeyTypeFrequencies.getCount(TAXI) / (double) sum : 0.0;
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
    journeyTypeFrequencies.addValue(CAR);
  }

  public void reportTransitJourney() {
    journeyTypeFrequencies.addValue(BUS);
  }

  public void reportBikeJourney() {
    journeyTypeFrequencies.addValue(BIKE);
  }

  public void reportWalkJourney() {
    journeyTypeFrequencies.addValue(WALK);
  }

  public void reportTaxiJourney() {
    journeyTypeFrequencies.addValue(TAXI);
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
    parkingSuccess.addValue(SUCCESS);
  }

  public int getSuccessfulParking() {
    return (int) parkingSuccess.getCount(SUCCESS);
  }

  public void reportFailedParking() {
    parkingSuccess.addValue(FAILURE);
  }

  public int getFailedParking() {
    return (int) parkingSuccess.getCount(FAILURE);
  }

  public int getReasonMaxSearchTimeExceeded() {
    return (int) parkingFailureReason.getCount(0);
  }

  public int getReasonNoPath() {
    return (int) parkingFailureReason.getCount(1);
  }

  public void reportSearchTimeParking(double searchTime) {
    parkingSpotSearchTime.addValue(searchTime);
  }

  public double getMeanSearchTimeParking() {
    return (parkingSpotSearchTime.getN() > 0) ? parkingSpotSearchTime.getMean() : 0.0;
  }

  public void reportParkingCosts(double costs) {
    parkingSpotCost.addValue(costs);
  }

  public double getMeanParkingCosts() {
    return (parkingSpotCost.getN() > 0) ? parkingSpotCost.getMean() : 0.0;
  }

  public void reportParkingWalkingDistance(double walkingDistance) {
    parkingSpotWalkingDistance.addValue(walkingDistance);
  }

  public double getMeanParkingWalkingDistance() {
    return (parkingSpotWalkingDistance.getN() > 0) ? parkingSpotWalkingDistance.getMean() : 0.0;
  }

  public void reportSearchUtility(double utility) {
    parkingSpotSearchUtility.addValue(utility);
  }

  public double getMeanUtilityParking() {
    return (parkingSpotSearchUtility.getN() > 0) ? parkingSpotSearchUtility.getMean() : 0.0;
  }

  public void updateGlobalStatistics(Context simulationContext) {
    Collection<Entity> persons = simulationContext.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
    updateMeanTransportPreferences(persons);

    if (coverageStats != null)
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

      /*
       * if (p.getBusPreference() >= p.getCarPreference()) { busPrefAcc++; }
       * else { carPrefAcc++; }
       */
      busPrefAcc += p.getBusPreference();
      carPrefAcc += p.getCarPreference();
    }
    meanBusPreference = busPrefAcc / persons.size();
    meanCarPreference = carPrefAcc / persons.size();
  }
}