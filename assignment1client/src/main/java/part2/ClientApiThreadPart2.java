package part2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.Logger;

public class ClientApiThreadPart2 extends Thread {
  private CountDownLatch countDownLatch;
  private int startRange;
  private int endRange;
  private int startTime;
  private int endTime;
  private int callGetTime;
  private int callPostTime;
  private int skiDayNum;
  private int numLifts;
  private String resortID;
  private String serverAddress;
  private Logger logger;

  private int successfulRequestNum = 0;
  private int unsuccessfulRequestNum = 0;
  private List<Integer> codeList = new ArrayList<>();
  private List<Long> startTimeList = new ArrayList<>();
  private List<String> typeList = new ArrayList<>();
  private List<Long> latencyList = new ArrayList<>();

  private int phase;

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

  public ClientApiThreadPart2(CountDownLatch countDownLatch, int startRange, int endRange, int startTime,
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
    // Api call
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(serverAddress);

    // post
    for (int i = 0; i < callPostTime; i++) {
      long phaseStartTime = System.currentTimeMillis();;
      try {
        LiftRide liftRide = getListRide();
        ApiResponse res = apiInstance.writeNewLiftRideWithHttpInfo(liftRide);
        long latency = System.currentTimeMillis() - phaseStartTime;
        int code = res.getStatusCode();
        this.codeList.add(code);
        this.typeList.add("POST");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);

        if (code >= 400) {
          logger.error("thead POST done with error status " + code);
          this.unsuccessfulRequestNum++;
        } else {
          this.successfulRequestNum++;
        }

      } catch (ApiException ae) {
        long latency = System.currentTimeMillis() - phaseStartTime;
        int code = ae.getCode();
        this.codeList.add(code);
        this.typeList.add("POST");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);
        logger.error("ApiException: phase " + phase + " POST, response code" + code, ae);
        this.unsuccessfulRequestNum++;
      }
    }

    // get
    for (int i = 0; i < callGetTime; i++) {
      long phaseStartTime = System.currentTimeMillis();
      try {
        int skierId = startRange + (int) Math.ceil(Math.random() * (endRange - startRange));
        ApiResponse res = apiInstance.getSkierDayVerticalWithHttpInfo(resortID, skiDayNum + "", skierId + "");
        long latency = System.currentTimeMillis() - phaseStartTime;

        // add to successful counter
        int code = res.getStatusCode();
        this.codeList.add(code);
        this.typeList.add("GET");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);

        if (code >= 400) {
          logger.error("thread GET done with error status " + code);
          this.unsuccessfulRequestNum++;
        } else {
          this.successfulRequestNum++;
        }
      } catch (ApiException ae) {
        // add to unsuccessful counter
        long latency = System.currentTimeMillis() - phaseStartTime;
        int code = ae.getCode();
        this.codeList.add(code);
        this.typeList.add("POST");
        this.latencyList.add(latency);
        this.startTimeList.add(phaseStartTime);
        logger.error("ApiException: phase " + phase + " GET, response code" + code, ae);
        this.unsuccessfulRequestNum++;
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
