import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.ResortsApi;

import java.io.File;
import java.util.*;

public class ResortsApiExample {

  public static void main(String[] args) {

    ResortsApi apiInstance = new ResortsApi();
    ApiClient client = apiInstance.getApiClient();
//    client.setBasePath("http://localhost:8080/distributed_system_war_exploded");
//    client.setBasePath("http://localhost:8080/ds_war_exploded");
    client.setBasePath("http://ec2-34-229-151-1.compute-1.amazonaws.com:8080/ds_war");

    List<String> resort = Arrays.asList("resort_example"); // List<String> | resort to query by
//    List<String> dayID = Arrays.asList("dayID_example"); // List<String> | day number in the season
    List<String> dayID = Arrays.asList("1");
    try {
      TopTen result = apiInstance.getTopTenVert(resort, dayID);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#getTopTenVert");
      e.printStackTrace();
    }
  }
}