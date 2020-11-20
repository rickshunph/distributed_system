package part1;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client {

  private static final Logger logger = LogManager.getLogger("DS_Client");

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientGetPropertyValues properties = new ClientGetPropertyValues();
    String[] defaultArgs = properties.getPropValues();
    logger.info("List of input arguments" + Arrays.toString(defaultArgs));
    int[] maxThreadSet = new int[]{32, 64, 128, 256};
//    int[] maxThreadSet = new int[]{600};

    try {
      Set<ClientApiThread> set = new HashSet<>();
      int maxThreads = Integer.parseInt(defaultArgs[0]);
      int numSkiers = Integer.parseInt(defaultArgs[1]);
      int numLifts = Integer.parseInt(defaultArgs[2]);
      int skiDayNum = Integer.parseInt(defaultArgs[3]);
      String resortID = defaultArgs[4];
      String serverAddress = defaultArgs[5];

      // maxThreads 256
      if (maxThreads > 256) {
        logger.error("IllegalArgumentException: maxThreads should be less than or equal to 256",
            new IllegalArgumentException("maxThreads should be less than or equal to 256"));
      }

      // numLifts should be in range 5-60
      if (numLifts < 5 || numLifts > 60) {
        logger.error("IllegalArgumentException: numLifts should be in range 5-60",
            new IllegalArgumentException("numLifts should be in range 5-60"));
      }

//      for (int j = 0; j < maxThreadSet.length; j++) {
//        maxThreads = maxThreadSet[j];
        long phaseStartTime = System.currentTimeMillis();

        // phase 1: start up, launch (maxThreads/4) threads
        int phaseOneThreadNum = maxThreads / 4;
        CountDownLatch phaseOneCompleted = new CountDownLatch(
            (int) Math.ceil(0.1 * phaseOneThreadNum));
        phase(phaseOneCompleted, phaseOneThreadNum, numSkiers, 1, 90,
            5, 100, 1, skiDayNum, numLifts, resortID,
            serverAddress, set);
        phaseOneCompleted.await();

        // phase 2: peak
        int phaseTwoThreadNum = maxThreads;
        CountDownLatch phaseTwoCompleted = new CountDownLatch(
            (int) Math.ceil(0.1 * phaseTwoThreadNum));
        phase(phaseTwoCompleted, phaseTwoThreadNum, numSkiers, 91, 360,
            5, 100, 2, skiDayNum, numLifts, resortID,
            serverAddress, set);
        phaseTwoCompleted.await();

        // phase 3: cooldown
        int phaseThreeThreadNum = maxThreads / 4;
        CountDownLatch phaseThreeCompleted = new CountDownLatch(
            (int) Math.ceil(phaseThreeThreadNum));
        phase(phaseThreeCompleted, phaseThreeThreadNum, numSkiers, 361, 420,
            10, 100, 3, skiDayNum, numLifts, resortID,
            serverAddress, set);
        phaseThreeCompleted.await();
        long phaseEndTime = System.currentTimeMillis();

        double wallTime = ((double) (phaseEndTime - phaseStartTime)) / 1000.0;

        int successful = 0;
        int unsuccessful = 0;
        for (ClientApiThread thread : set) {
          successful += thread.getSuccessfulRequestNum();
          unsuccessful += thread.getUnsuccessfulRequestNum();
        }

        // output stat
        System.out.println("Running max " + maxThreads + " threads");
        System.out.println("Number of successful requests sent: " + successful);
        System.out.println("Number of unsuccessful requests sent: " + unsuccessful);
        System.out.println("Total run time: " + (wallTime) + "s");
        System.out.println("throughput = requests per second = " +
            (successful / wallTime));
        System.out.println();

        logger.info("Done!");
//      }

    } catch (NumberFormatException nfe) {
      logger.error("NumberFormatException: Input in properties file or command line argument"
          + "should be numbers");
    }
  }

  private static void phase(CountDownLatch completed, int ThreadNum, int numSkiers, int startTime,
      int endTime, int callGetTime, int callPostTime, int phase, int skiDayNum, int numLifts,
      String resortID, String serverAddress, Set<ClientApiThread> set) {
    for (int i = 0; i < ThreadNum; i++) {
      int startRange = i * numSkiers + 1;
      int endRange = (i + 1) * numSkiers;
      ClientApiThread thread = new ClientApiThread(completed, startRange, endRange,
          startTime, endTime, callGetTime, callPostTime, skiDayNum, numLifts, resortID, phase,
          serverAddress, logger);
      set.add(thread);
      thread.start();
    }
  }
}