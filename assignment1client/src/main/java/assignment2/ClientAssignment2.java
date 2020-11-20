package assignment2;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import part1.ClientGetPropertyValues;

public class ClientAssignment2 {

  private static final Logger logger = LogManager.getLogger("DS_Client");
  private static final String CSV_FILE_NAME = "output_csv";
  private static final String CSV_FILE_SURFIX = ".csv";

  public static void main(String[] args) throws IOException, InterruptedException {
    ClientGetPropertyValues properties = new ClientGetPropertyValues();
    String[] defaultArgs = properties.getPropValues();
    logger.info("List of input arguments" + Arrays.toString(defaultArgs));
//    long[] throughputSet = new long[4];
//    long[] meanResponseTimePostSet = new long[4];
//    long[] meanResponseTimeGetSet = new long[4];

    try {
      Set<ClientApiThreadAssignment2> set = new HashSet<>();
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

      long phaseStartTime = System.currentTimeMillis();

      // phase 1: start up, launch (maxThreads/4) threads
      System.out.println("");
      int phaseOneThreadNum = maxThreads / 4;
      CountDownLatch phaseOneCompleted = new CountDownLatch(
          (int) Math.ceil(0.1 * phaseOneThreadNum));
      phase(phaseOneCompleted, phaseOneThreadNum, numSkiers, 1, 90,
          5, 1000, 1, skiDayNum, numLifts, resortID,
          serverAddress, set);
      phaseOneCompleted.await();

      // phase 2: peak
      int phaseTwoThreadNum = maxThreads;
      CountDownLatch phaseTwoCompleted = new CountDownLatch(
          (int) Math.ceil(0.1 * phaseTwoThreadNum));
      phase(phaseTwoCompleted, phaseTwoThreadNum, numSkiers, 91, 360,
          5, 1000, 2, skiDayNum, numLifts, resortID,
          serverAddress, set);
      phaseTwoCompleted.await();

      // phase 3: cooldown
      int phaseThreeThreadNum = maxThreads / 4;
      CountDownLatch phaseThreeCompleted = new CountDownLatch(
          (int) Math.ceil(phaseThreeThreadNum));
      phase(phaseThreeCompleted, phaseThreeThreadNum, numSkiers, 361, 420,
          10, 1000, 3, skiDayNum, numLifts, resortID,
          serverAddress, set);
      phaseThreeCompleted.await();
      long phaseEndTime = System.currentTimeMillis();

      long wallTime = (phaseEndTime - phaseStartTime) / 1000;

      // GET: SkierDayVertical /skiers/{resortID}/days/{dayID}/skiers/{skierID}
      // GET2: SkierResortTotals /skiers/{skierID}/vertical
      int successful = 0;
      int unsuccessful = 0;
      int postFail = 0; /////////////////////
      long min = Integer.MAX_VALUE;
      int totalGet = 0;
      int totalGet2 = 0;
      int totalPost = 0;
      long maxPost = -1;
      long maxGet = -1;
      long maxGet2 = -1;
      int totalResponseTime = 0;
      List<Long> getResponseTime = new ArrayList<>();
      List<Long> getResponseTime2 = new ArrayList<>();
      List<Long> postResponseTime = new ArrayList<>();
      List<String[]> dataLines = new ArrayList<>();
      for (ClientApiThreadAssignment2 thread : set) {
        int size = thread.getLatencyList().size();
        List<Long> latency = thread.getLatencyList();
        List<Integer> code = thread.getCodeList();
        List<Long> startTime = thread.getStartTimeList();
        List<String> resType = thread.getTypeList();
        successful += thread.getSuccessfulRequestNum();
        unsuccessful += thread.getUnsuccessfulRequestNum();
        postFail += thread.getPostUnsuccess();
        for (int i = 0; i < size; i++) {
          long responseTime = latency.get(i);

          dataLines.add(new String[]{startTime.get(i) + "", resType.get(i),
              responseTime + "", code.get(i) + ""});

          totalResponseTime += responseTime;

          if (resType.get(i).equalsIgnoreCase("POST")) {
            totalPost += responseTime;
            postResponseTime.add(responseTime);
            min = min < responseTime ? min : latency.get(i);
            maxPost = Math.max(responseTime, maxPost);
          } else if (resType.get(i).equalsIgnoreCase("GET")) { // GET
            totalGet += responseTime;
            getResponseTime.add(responseTime);
            maxGet = Math.max(responseTime, maxGet);
          } else {
            totalGet2 += responseTime;
            getResponseTime2.add(responseTime);
            maxGet2 = Math.max(responseTime, maxGet);
          }
        }
      }
      Collections.sort(postResponseTime);
      Collections.sort(getResponseTime);

      long postMedian = calculateMedian(postResponseTime);
      long getMedian = calculateMedian(getResponseTime);
      long getMedian2 = calculateMedian(getResponseTime2);

      // output stat
      System.out.println("GET: SkierDayVertical /skiers/{resortID}/days/{dayID}/skiers/{skierID}");
      System.out.println("GET2: SkierResortTotals /skiers/{skierID}/vertical");

      System.out.println();

      System.out.println("maxThread: " + maxThreads);
      System.out.println("Number of successful requests sent: " + successful);
      System.out.println("Number of unsuccessful requests sent: " + unsuccessful);
      System.out.println("Number of Failed post requests: " + postFail);
      System.out.println("Total run time: " + (wallTime) + "s");
      long throughput = (successful / wallTime);
      System.out.println("throughput = requests per second = " +
          throughput);

      System.out.println();

      System.out.println("min latency: " + min);
      System.out.println("max GET response time: " + maxGet);
      System.out.println("max GET2 response time: " + maxGet2);
      System.out.println("max POST response time: " + maxPost);

      long meanResponseTimeGet = (totalGet / getResponseTime.size());
      long meanResponseTimeGet2 = (totalGet2 / getResponseTime2.size());
      long meanResponseTimePost = (totalPost / postResponseTime.size());
      System.out.println("mean GET response time: " + (1.0 * meanResponseTimeGet));
      System.out.println("mean GET2 response time: " + (1.0 * meanResponseTimeGet2));
      System.out.println("mean POST response time: " + (1.0 * meanResponseTimePost));
      System.out.println("total mean response time: "
          + (totalResponseTime / (getResponseTime.size() + getResponseTime2.size() + postResponseTime.size())));

      System.out.println("median GET response time: " + getMedian);
      System.out.println("median GET response time: " + getMedian2);
      System.out.println("median POST response time: " + postMedian);
      System.out.println("99 percentiles GET: " + getResponseTime
          .get((int) Math.floor(getResponseTime.size() * 0.99)));
      System.out.println("99 percentiles GET2: " + getResponseTime2
          .get((int) Math.floor(getResponseTime2.size() * 0.99)));
      System.out.println("99 percentiles POST: " + postResponseTime
          .get((int) Math.floor(postResponseTime.size() * 0.99)));

      System.out.println();

      System.out.println("Done!");
      logger.info("Done!");

      File csvOutputFile = new File(CSV_FILE_NAME + "_thread_" + maxThreads +
          CSV_FILE_SURFIX);
      try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
        dataLines.stream()
            .map(ClientAssignment2::convertToCSV)
            .forEach(pw::println);
      }


    } catch (
        NumberFormatException nfe) {
      logger.error("NumberFormatException: Input in properties file or command line argument"
          + "should be numbers");
    }

  }

  public static String convertToCSV(String[] data) {
    return Stream.of(data)
        .map(ClientAssignment2::escapeSpecialCharacters)
        .collect(Collectors.joining(","));
  }

  public static String escapeSpecialCharacters(String data) {
    String escapedData = data.replaceAll("\\R", " ");
    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
      data = data.replace("\"", "\"\"");
      escapedData = "\"" + data + "\"";
    }
    return escapedData;
  }

  private static long calculateMedian(List<Long> list) {
    long median = -1;
    if (list.size() % 2 == 0) {
      median = (list.get(list.size() / 2) +
          list.get(list.size() / 2 - 1)) / 2;
    } else {
      median = list.get(list.size() / 2);
    }
    return median;
  }

  private static void phase(CountDownLatch completed, int ThreadNum, int numSkiers, int startTime,
      int endTime, int callGetTime, int callPostTime, int phase, int skiDayNum, int numLifts,
      String resortID, String serverAddress, Set<ClientApiThreadAssignment2> set) {
    for (int i = 0; i < ThreadNum; i++) {
      int startRange = i * numSkiers + 1;
      int endRange = (i + 1) * numSkiers;
      ClientApiThreadAssignment2 thread = new ClientApiThreadAssignment2(completed, startRange, endRange,
          startTime, endTime, callGetTime, callPostTime, skiDayNum, numLifts, resortID, phase,
          serverAddress, logger);
      set.add(thread);
      thread.start();
    }
  }
}
