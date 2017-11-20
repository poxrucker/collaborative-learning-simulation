package de.dfki.parking.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface IParkingDataFileParser {

  Map<String, List<ParkingData>> parseFile(Path fqfn) throws IOException;
  
}
