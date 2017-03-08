package allow.simulator.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;

import allow.simulator.world.Street;

public final class StreetCapacityLogger {

	private Context context;
	private LocalDateTime lastWrite;
	private int samplingRatesInSeconds;
	private BufferedWriter writer;
	
	public StreetCapacityLogger(Context context, int samplingRateInSeconds) {
		this.context = context;
		this.samplingRatesInSeconds = samplingRateInSeconds;
	}
	
	public void start(Path dest) throws IOException {
		if (writer != null)
			writer.close();
		
		writer = Files.newBufferedWriter(dest);
	}
	
	public void close() throws IOException {
		if (writer == null)
			return;
		
		writer.close();
		writer = null;
	}
	
	public boolean log(Collection<Street> streets) throws IOException {
		if (writer == null)
			throw new IllegalStateException("Writer is not initialized");
		
		boolean first = (lastWrite == null);
		
		if (first) {
			// Write header
			StringBuilder header = new StringBuilder();
			header.append("seconds;");
			
			for (Iterator<Street> it = streets.iterator(); it.hasNext();) {
				Street street = it.next();
				header.append(street.getId());
				
				if (it.hasNext())
					header.append(";");
			}
			header.append("\n");
			writer.write(header.toString());
			lastWrite = context.getTime().getCurrentDateTime();
		}
		
		if (!first && lastWrite.until(context.getTime().getCurrentDateTime(), ChronoUnit.SECONDS) <= (samplingRatesInSeconds - 1))
			return false;
		
		StringBuilder strb = new StringBuilder();
		long t = context.getTime().getNTicks() * context.getTime().getDeltaT();
		strb.append(t + ";");
		
		for (Iterator<Street> it = streets.iterator(); it.hasNext();) {
			Street street = it.next();
			strb.append(street.getUsageStatistics()[0]);
			
			if (it.hasNext())
				strb.append(";");
			// strb.append(t + ";" + street.getId() + ";" + street.getName() + ";" + street.getUsageStatistics()[0] + "\n");
		}
		strb.append("\n");
		writer.write(strb.toString());
		writer.flush();
		lastWrite = context.getTime().getCurrentDateTime();
		return true;
	}
}
