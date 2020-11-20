import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;

public class Test {

  public static void main(String[] args) {
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
    System.out.println(client.getConnectTimeout());
  }
}
