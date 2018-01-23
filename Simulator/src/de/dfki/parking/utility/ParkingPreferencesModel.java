package de.dfki.parking.utility;


public final class ParkingPreferencesModel {
  //Weight for parking cost
 private double cweight;
 
 // Weight for walking distance
 private double wdweight;
 
 // Weight for search time
 private double stweight;
 
 // Maximum acceptable cost for parking
 private double cmax;
 
 // Maximum acceptable walking distance from parking to final destination
 private double wdmax;
 
 // Maximum acceptable searching time for parking
 private double stmax;
 
 public ParkingPreferencesModel(double cweight, double wdweight, double stweight, 
     double cmax, double wdmax, double stmax) {
   this.cweight = cweight;
   this.wdweight = wdweight;
   this.stweight = stweight;
   this.cmax = cmax;
   this.wdmax = wdmax;
   this.stmax = stmax;
 }

 /**
  * Returns the weight for costs.
  * 
  * @return Weight for costs
  */
 public double sampleCWeight() {
   return cweight;
 }

 /**
  * Returns the weight for walking distance from parking spot to destination.
  * 
  * @return Weight for walking distance
  */
 public double sampleWdWeight() {
   return wdweight;
 }

 /**
  * Returns the weight for parking spot search time.
  * 
  * @return Weight for parking spot search time
  */
 public double sampleStWeight() {
   return stweight;
 }

 /**
  * Returns the preferred maximum costs for parking.
  * 
  * @return Preferred maximum costs
  */
 public double sampleCMax() {
   return cmax;
 }

 /**
  * Sets the preferred maximum costs for parking.
  * 
  * @param cmax Preferred maximum costs
  */
 public void sampleCMax(double cmax) {
   this.cmax = cmax;
 }

 /**
  * Returns the preferred maximum walking distance from parking spot to destination.
  * 
  * @return Preferred maximum walking distance
  */
 public double sampleWdMax() {
   return wdmax;
 }

 /**
  * Returns the preferred maximum search time for parking spot.
  * 
  * @return Preferred maximum search time
  */
 public double sampleStMax() {
   return stmax;
 }
}