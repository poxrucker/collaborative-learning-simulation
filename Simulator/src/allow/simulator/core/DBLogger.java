package allow.simulator.core;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;

import scala.actors.threadpool.Arrays;
import allow.simulator.entity.Entity;
import allow.simulator.entity.EntityTypes;
import allow.simulator.entity.Person;
import allow.simulator.entity.Profile;
import allow.simulator.entity.TravelEvent;
import allow.simulator.flow.activity.Activity;
import allow.simulator.flow.activity.ActivityType;
import allow.simulator.util.Coordinate;
import allow.simulator.world.overlay.RasterOverlay;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;

public final class DBLogger {

	private Context context;
	private LocalDateTime lastWrite;
	private int samplingRateInMinutes;
	private MongoClient client;
	private DBConfiguration config;
	
	public DBLogger(Context context, int samplingRateInMinutes, DBConfiguration config) throws UnknownHostException {
		this.context = context;
		this.samplingRateInMinutes = samplingRateInMinutes;
		this.config = config;
		List<MongoCredential> temp = new ArrayList<MongoCredential>(1);
		temp.add(MongoCredential.createCredential(config.getUser(), config.getDBName(), config.getPassword().toCharArray()));
		client = new MongoClient(new ServerAddress(), temp);
	}
	
	public void writePopulation() {
		// Connect to database
        MongoDatabase database = client.getDatabase(config.getDBName());
        MongoCollection<Document> collection = database.getCollection("profiles");	
        
		Collection<Entity> entities = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
		List<WriteModel<Document>> inserts = new ArrayList<WriteModel<Document>>(entities.size());
		
		for (Entity entity : entities) {
			Person person = (Person) entity;
			Document doc = createPopulationDocument(person);
			inserts.add(new InsertOneModel<Document>(doc));
		}
		collection.bulkWrite(inserts, new BulkWriteOptions().ordered(false));
	}
	
	public void writePopulationSample() {
		boolean first = (lastWrite == null);
		
		if (first)
			lastWrite = context.getTime().getCurrentDateTime();
		
		if (!first && lastWrite.until(context.getTime().getCurrentDateTime(), ChronoUnit.MINUTES) <= (samplingRateInMinutes - 1))
			return;
		
		// Connect to database
        MongoDatabase database = client.getDatabase(config.getDBName());
        MongoCollection<Document> collection = database.getCollection("diary");	
        
		Collection<Entity> entities = context.getEntityManager().getEntitiesOfType(EntityTypes.PERSON);
		List<WriteModel<Document>> inserts = new ArrayList<WriteModel<Document>>(entities.size());

		for (Entity entity : entities) {
			Person person = (Person) entity;
			Document doc = createPersonSampleDocument(person);
			inserts.add(new InsertOneModel<Document>(doc));
		}
		collection.bulkWrite(inserts, new BulkWriteOptions().ordered(false));
		lastWrite = context.getTime().getCurrentDateTime();
	}
	
	public void close() {
		client.close();
	}
	
	private Document createPersonSampleDocument(Person person) {
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
		// Get raster for distance sampling
		RasterOverlay raster = (RasterOverlay) context.getWorld().getOverlay(Simulator.OVERLAY_RASTER);

		Document doc = new Document("user_id", person.getId())
			.append("timestamp", context.getTime().getTimestamp() / 1000)
			.append("location", Arrays.asList(new Double[] { person.getPosition().x, person.getPosition().y }))
			.append("activity", activityString)
			.append("crowd_density_2", raster.getCloseEntities(person.getPosition(), 2).size())
			.append("crowd_density_5", raster.getCloseEntities(person.getPosition(), 5).size())
			.append("crowd_density_10", raster.getCloseEntities(person.getPosition(), 10).size());
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	private Document createPopulationDocument(Person person) {
		// Get destinations
		List<List<Double>> destinations = new ArrayList<List<Double>>();
					
		for (int i = 0; i < 7; i++) {
			List<TravelEvent> events = person.getDailyRoutine().getDailyRoutine(i + 1);
						
			if (events.size() == 0)
				continue;
			Coordinate dest = events.get(0).getDestination();
			destinations.add(Arrays.asList(new Double[] { dest.x, dest.y }));
		}
					
		Document doc = new Document("_id", person.getId())
			.append("user_id", person.getId())
			.append("gender", person.getGender().toString())
			.append("home_location", Arrays.asList(new Double[] { person.getHome().x, person.getHome().y }))
			.append("type", person.getProfile().toString())
			.append("work_locations", destinations);
		return doc;
	}
}
