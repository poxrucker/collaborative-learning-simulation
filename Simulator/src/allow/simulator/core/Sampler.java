package allow.simulator.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityType;
import allow.simulator.entity.Person;
import allow.simulator.entity.Profile;
import allow.simulator.entity.TravelEvent;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.util.Coordinate;
import allow.simulator.world.overlay.RasterOverlay;

public class Sampler {

	private static DateTimeFormatter format = DateTimeFormatter.ofPattern("dd_MM_yyyy");

	private Context context;
	private LocalDateTime lastWrite;
	private int samplingRateInMinutes;
	
	public Sampler(Context context, int samplingRateInMinutes) {
		this.context = context;
		this.samplingRateInMinutes = samplingRateInMinutes;
	}
	
	public void writePopulation(Path path) throws IOException {
		Path samplePath = path.resolve("population.txt");
		BufferedWriter writer = Files.newBufferedWriter(samplePath);
		writer.write("id,gender,home_lon,home_lat,profile,[work_lon;work_lat]\n");
		
		Collection<Entity> entities = context.getEntityManager().getEntitiesOfType(EntityType.PERSON);

		for (Entity entity : entities) {
			Person person = (Person) entity;
			String destinations = "[";
			boolean added = false;
			
			for (int i = 0; i < 7; i++) {
				List<TravelEvent> events = person.getDailyRoutine().getDailyRoutine(i + 1);
				
				if (events.size() == 0)
					continue;
				
				if (added)
					destinations += ";";
				
				Coordinate dest = events.get(0).getDestination();
				destinations += dest.x + ";" + dest.y;
				added = true;
			}
			destinations += "]";
			
			String line = person.getId() + "," + person.getGender() + "," + person.getHome().x + "," 
					+ person.getHome().y + "," + person.getProfile() + "," + destinations;
			writer.write(line + "\n");
		}
		writer.close();
	}
	
	public void writePopulationSample(Path path) throws IOException {
		boolean first = (lastWrite == null);
		
		if (first)
			lastWrite = context.getTime().getCurrentDateTime();
		
		if (!first && lastWrite.until(context.getTime().getCurrentDateTime(), ChronoUnit.MINUTES) <= (samplingRateInMinutes - 1))
			return;
		
		Path samplePath = path.resolve("day_" + context.getTime().getDays() + ".txt");
		boolean exists = Files.exists(samplePath);
		
		if (!exists)
			Files.createFile(samplePath);
		
		BufferedWriter writer = Files.newBufferedWriter(samplePath, Charset.defaultCharset(), StandardOpenOption.APPEND);
		
		if (!exists)
			writer.write("id,timestamp,loc_lon,loc_lat,activity,density_2,density_5,density_10\n");
		
		Collection<Entity> entities = context.getEntityManager().getEntitiesOfType(EntityType.PERSON);
		RasterOverlay raster = (RasterOverlay) context.getWorld().getOverlay(Simulator.OVERLAY_RASTER);
		
		for (Entity entity : entities) {
			Person person = (Person) entity;
			Activity activity = person.getFlow().getCurrentActivity();
			String activityString = "NA";
			
			if (activity == null || !(activity.getType() == ActivityType.CYCLE && activity.getType() == ActivityType.DRIVE)
					&& activity.getType() == ActivityType.USE_FLEXIBUS && activity.getType() == ActivityType.USE_PUBLIC_TRANSPORT
					&& activity.getType() == ActivityType.USE_TAXI && activity.getType() == ActivityType.WAIT
					&& activity.getType() == ActivityType.WALK) {
				
				if (person.isAtHome()) {
					activityString = "HOME";
					
				} else if (person.getProfile() == Profile.WORKER) {
					Coordinate work = person.getDailyRoutine().getDailyRoutine(1).get(0).getDestination();
					
					if (work.equals(person.getPosition()))
						activityString = "WORK";
					
				} else if (person.getProfile() == Profile.STUDENT) {
					Coordinate work = person.getDailyRoutine().getDailyRoutine(1).get(0).getDestination();
					
					if (work.equals(person.getPosition()))
						activityString = "UNIVERSITY";
					
				} else if (person.getProfile() == Profile.CHILD) {
					Coordinate work = person.getDailyRoutine().getDailyRoutine(1).get(0).getDestination();
					
					if (work.equals(person.getPosition()))
						activityString = "SCHOOL";
					
				}
				
			} else {
				activityString = activity.getType().name();
			}
			int density_2 = raster.getCloseEntities(person.getPosition(), 2).size();
			int density_5 = raster.getCloseEntities(person.getPosition(), 5).size();
			int density_10 = raster.getCloseEntities(person.getPosition(), 10).size();
			String line = person.getId() + "," + context.getTime().getTimestamp() + "," + person.getPosition().x + ","
					+ person.getPosition().y + "," + activityString + "," + density_2 + "," + density_5 + "," + density_10;
			writer.write(line + "\n");
		}
		writer.close();
		lastWrite = context.getTime().getCurrentDateTime();
	}
	
}
