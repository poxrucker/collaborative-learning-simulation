package de.dfki.parking.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IParkingDataFileParser {

  List<ParkingData> parseFile(Path fqfn) throws IOException;
  
}
