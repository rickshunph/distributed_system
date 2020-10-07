import io.swagger.client.*;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

import java.io.File;
import java.util.*;

public class SkiersApiExample {

  public static void main(String[] args) {

    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
//    client.setBasePath("http://localhost:8080/distributed_system_war_exploded");
    client.setBasePath("http://ec2-34-229-151-1.compute-1.amazonaws.com:8080/ds_war");

    String resortID = "resortIdExample";
    String dayID = "dayIdExample";
    String skierID = "skierIdExample";
    try {
      SkierVertical result = apiInstance.getSkierDayVertical(resortID, dayID, skierID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SkiersApi#getSkierDayVertical");
      e.printStackTrace();
    }

    try {
      List<String> resort = Arrays.asList("resort_example");

      SkierVertical result = apiInstance.getSkierResortTotals(skierID, resort);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SkiersApi#getSkierDayVertical");
      e.printStackTrace();
    }
  }
}
