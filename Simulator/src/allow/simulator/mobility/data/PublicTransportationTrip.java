package allow.simulator.mobility.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import allow.simulator.mobility.data.gtfs.GTFSServiceException;
import allow.simulator.world.Street;

public final class PublicTransportationTrip extends Trip {
	// Id of trip.
		private LocalDate startingDate;
		private LocalDate endingDate;
		private List<GTFSServiceException> exceptions;
		
		/**
		 * Constructor.
		 * Creates a new trip with given Id, stops, and trace.
		 * 
		 * @param tripId Id of the trip.
		 * @param schedule Schedule of the trip including stop and stop times.
		 * @param trace Trace between stops.
		 */
		public PublicTransportationTrip(String tripId, LocalDate starting, LocalDate ending,
				List<GTFSServiceException> exceptions, 
				List<BusStop> stops,
				List<LocalTime> stopTimes,
				List<List<Street>> trace) {
			super(tripId, new ArrayList<Stop>(stops), stopTimes, trace);
			this.startingDate = starting;
			this.endingDate = ending;
			this.exceptions = exceptions;
		}
		
		public List<BusStop> getStops() {
			List<BusStop> ret = new ArrayList<BusStop>(stops.size());
			
			for (Stop s : stops) {
				ret.add((BusStop) s);
			}
			return ret;
		}
		
		public boolean isValidThisDay(LocalDate day) {
			boolean isValid = day.compareTo(startingDate) >= 0 && day.compareTo(endingDate) <= 0;
			
			for (int i = 0; i < exceptions.size(); i++) {
				isValid = isValid && day.compareTo(exceptions.get(i).getDate()) != 0;
			}
			return isValid;
		}
		
		public String toString() {
			return tripId + " " + startingDate + " " + endingDate;
		}
}
