package assignment2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.Logger;

public class ClientApiThreadAssignment2 extends Thread {
  private final CountDownLatch countDownLatch;
  private final int startRange;
  private final int endRange;
  private final int startTime;
  private final int endTime;
  private final int callGetTime;
  private final int callPostTime;
  private final int skiDayNum;
  private final int numLifts;
  private final String resortID;
  private final String serverAddress;
  private final Logger logger;

  private int successfulRequestNum = 0;
  private int unsuccessfulRequestNum = 0;
  private int postUnsuccess = 0; ///////////////////////////////////////
  private final List<Integer> codeList = new ArrayList<>();
  private final List<Long> startTimeList = new ArrayList<>();
  private final List<String> typeList = new ArrayList<>();
  private final List<Long> latencyList = new ArrayList<>();

  private final int phase;

  public int getSuccessfulRequestNum() {
    return successfulRequestNum;
  }

  public int getUnsuccessfulRequestNum() {
    return unsuccessfulRequestNum;
  }

  public List<Integer> getCodeList() {
    return codeList;
  }

  public List<Long> getStartTimeList() {
    return startTimeList;
  }

  public List<String> getTypeList() {
    return typeList;
  }

  public List<Long> getLatencyList() {
    return latencyList;
  }

  public int getPostUnsuccess() {
    return postUnsuccess;
  }

  public ClientApiThreadAssignment2(CountDownLatch countDownLatch, int startRange, int endRange, int startTime,
      int endTime, int callGetTime, int callPostTime, int skiDayNum, int numLifts, String resortID,
      int phase, String serverAddress, Logger logger) {
    this.countDownLatch = countDownLatch;
    this.logger = logger;
    this.startRange = startRange;
    this.endRange = endRange;
    this.startTime = startTime;
    this.endTime = endTime;
    this.callGetTime = callGetTime;
    this.callPostTime = callPostTime;
    this.skiDayNum = skiDayNum;
    this.numLifts = numLifts;
    this.resortID = resortID;
    this.serverAddress = serverAddress;

    this.phase = phase;
  }

  public void run() {
//    logger.info("start a thread");
    // Api call
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(serverAddress);
    client.setConnectTimeout(20000);  ////////////////////////////////////////////////
//    client.setConnectTimeout(20);

    // post
    for (int i = 0; i < callPostTime; i++) {
      long phaseStartTime = System.currentTimeMillis();
      try {
        LiftRide liftRide = getListRide();
        ApiResponse res = apiInstance.writeNewLiftRideWithHttpInfo(liftRide);
        long latency = System.currentTimeMillis() - phaseStartTime;
        int code = res.getStatusCode();
        this.codeList.add(code);
        this.typeList.add("POST");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);

        if (code != 201) {
          logger.error("thead POST done with error status " + code);
        }
        this.successfulRequestNum++;
      } catch (ApiException ae) {
        long latency = System.currentTimeMillis() - phaseStartTime;
        int code = ae.getCode();
        this.codeList.add(code);
        this.typeList.add("POST");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);
//        System.out.println("POST error");
        logger.error("ApiException: phase " + phase + " POST, response code" + code, ae);
        this.unsuccessfulRequestNum++;
        this.postUnsuccess++;
      }
    }

    client.setConnectTimeout(8000);
    // get
    for (int i = 0; i < callGetTime; i++) {
      int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
      try {

        long phaseStartTime = System.currentTimeMillis();
        ApiResponse res = apiInstance
            .getSkierDayVerticalWithHttpInfo(resortID, skiDayNum + "", skierId + "");
        long latency = System.currentTimeMillis() - phaseStartTime;

        // add to successful counter
        int code = res.getStatusCode();
        this.codeList.add(code);
        this.typeList.add("GET");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);

        if (code != 200) {
          logger.error("thread GET done with error status " + code);
//          this.unsuccessfulRequestNum++;
        }
        this.successfulRequestNum++;
      } catch (ApiException ae) {
        int attempt = 0;
        boolean success = false;
        long phaseStartTime = System.currentTimeMillis();
        while (attempt < 3 && !success) {
          try {
//            int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
            phaseStartTime = System.currentTimeMillis();
            ApiResponse res = apiInstance
                .getSkierDayVerticalWithHttpInfo(resortID, skiDayNum + "", skierId + "");
            long latency = System.currentTimeMillis() - phaseStartTime;

            // add to successful counter
            int code = res.getStatusCode();
            this.codeList.add(code);
            this.typeList.add("GET");
            this.latencyList.add(latency);
            this.startTimeList.add(phaseStartTime);

            if (code != 200) {
              logger.error("thread GET done with error status " + code);
            }
            this.successfulRequestNum++;
            success = true;
          } catch (ApiException ae2) {
            attempt++;
          }
        }
        // add to unsuccessful counter
        if (!success) {
          long latency = System.currentTimeMillis() - phaseStartTime;
          int code = ae.getCode();
          this.codeList.add(code);
          this.typeList.add("GET");
          this.latencyList.add(latency);
          this.startTimeList.add(phaseStartTime);
          System.out.println("GET error");
          logger.error("ApiException: phase " + phase + " GET, response code " + code + ", resortID = "
              + resortID + ", " + skiDayNum + "" + skierId, ae);
          this.unsuccessfulRequestNum++;
        }
      }
      if (this.phase == 3) {
        try {
//          int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
          long phaseStartTime = System.currentTimeMillis();
          ApiResponse res = apiInstance
              .getSkierResortTotalsWithHttpInfo(skierId + "", Arrays.asList(resortID));
          long latency = System.currentTimeMillis() - phaseStartTime;

          // add to successful counter
          int code = res.getStatusCode();
          this.codeList.add(code);
          this.typeList.add("GET2");
          this.latencyList.add(latency);
          this.startTimeList.add(phaseStartTime);

          if (code != 200) {
            logger.error("thread GET2 done with error status " + code);
//            this.unsuccessfulRequestNum++;
          }
          this.successfulRequestNum++;
        } catch (ApiException ae) {
          int attempt = 0;
          boolean success = false;
          long phaseStartTime = System.currentTimeMillis();
          while (attempt < 3 && !success) {
            try {
//              int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
              phaseStartTime = System.currentTimeMillis();
              ApiResponse res = apiInstance
                  .getSkierResortTotalsWithHttpInfo(skierId + "", Arrays.asList(resortID));
              long latency = System.currentTimeMillis() - phaseStartTime;

              // add to successful counter
              int code = res.getStatusCode();
              this.codeList.add(code);
              this.typeList.add("GET2");
              this.latencyList.add(latency);
              this.startTimeList.add(phaseStartTime);

              if (code != 200) {
                logger.error("thread GET2 done with error status " + code);
              }
              this.successfulRequestNum++;
              success = true;
            } catch (ApiException ae2) {
              attempt++;
            }
          }
          // add to unsuccessful counter
          if (!success) {
            long latency = System.currentTimeMillis() - phaseStartTime;
            int code = ae.getCode();
            this.codeList.add(code);
            this.typeList.add("GET2");
            this.latencyList.add(latency);
            this.startTimeList.add(phaseStartTime);
            System.out.println("GET2 error");
            logger.error("ApiException: phase " + phase + " GET2, response code" + code
                + ", skierId = " + skierId + ", resortID = " + resortID, ae);
            this.unsuccessfulRequestNum++;
          }
        }
      }
    }
    this.countDownLatch.countDown();
  }

  private LiftRide getListRide() {
    int time = startTime + (int) Math.ceil(Math.random() * (endTime - startTime));
    int liftId = (int) Math.ceil(Math.random() * (numLifts));
    int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
    LiftRide liftRide = new LiftRide();
    liftRide.setDayID(skiDayNum + "");
    liftRide.setLiftID(liftId + "");
    liftRide.setResortID(resortID);
    liftRide.setSkierID(skierId + "");
    liftRide.setTime(time + "");
    return liftRide;
  }
}
