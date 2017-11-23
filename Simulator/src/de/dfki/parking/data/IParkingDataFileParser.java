package de.dfki.parking.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface IParkingDataFileParser {

  /**
   * Loads ParkingData instances from the given file.
   * 
   * @param fqfn 
   * @return
   * @throws IOException
   */
  List<ParkingData> parseFile(Path fqfn) throws IOException;
  
}
