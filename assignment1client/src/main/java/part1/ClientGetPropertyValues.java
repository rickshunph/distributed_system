package part1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class ClientGetPropertyValues {
  String[] result = new String[6];
  InputStream inputStream;

  public String[] getPropValues() throws IOException {

    try {
      Properties prop = new Properties();
      String propFileName = "config.properties";

      inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

      if (inputStream != null) {
        prop.load(inputStream);
      } else {
        throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
      }

      Date time = new Date(System.currentTimeMillis());

      // get the property value and print it out
      String maxThreads = prop.getProperty("maxThreads");
      String numSkiers = prop.getProperty("numSkiers");
      String numLifts = prop.getProperty("numLifts");
      String skiDayNum = prop.getProperty("skiDayNum");
      String resortID = prop.getProperty("resortID");
      String serverAddress = prop.getProperty("serverAddress");

      this.result[0] = maxThreads == null ? "256" : maxThreads;
      this.result[1] = numSkiers == null ? "50000" : numSkiers;
      this.result[2] = numLifts == null ? "40" : numLifts;
      this.result[3] = skiDayNum;
      this.result[4] = resortID;
      this.result[5] = serverAddress;

//      System.out.println("maxThreads: " + maxThreads + ", numSkiers: " + numSkiers + ", numLifts: "
//          + numLifts + ", skiDayNum: " + skiDayNum + ", resortID: " + resortID + ", serverAddress: "
//          + serverAddress);
    } catch (Exception e) {
      System.out.println("Exception: " + e);
    } finally {
      inputStream.close();
    }
    return result;
  }
}