import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.github.f4b6a3.uuid.UuidCreator;
import io.swagger.client.model.LiftRide;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DynamoDAO {
  private AmazonDynamoDB client;
  private DynamoDB dynamoDB;
  private Table table;

  private static final String ACCESS_KEY = System.getProperty("ACCESS_KEY");
  private static final String SECRET_KEY = System.getProperty("SECRET_KEY");

  public DynamoDAO() {
    try {
      this.client = AmazonDynamoDBClientBuilder.standard()
          .withRegion(Regions.US_WEST_2)
          .withCredentials(new AWSStaticCredentialsProvider(
              new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
          .build();

      this.dynamoDB = new DynamoDB(client);

      this.table = dynamoDB.getTable("Skier");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void createLiftRide(LiftRide newLiftRide) {
    int skierId = Integer.parseInt(newLiftRide.getSkierID());
    String resortId = newLiftRide.getResortID();
    int dayId = Integer.parseInt(newLiftRide.getDayID());
    int time = Integer.parseInt(newLiftRide.getTime());
    int liftId = Integer.parseInt(newLiftRide.getLiftID());

    UpdateItemSpec updateItemSpec = updateItemSpec(skierId, resortId, liftId, dayId, time);
    try {
      // try to update
      System.out.println("Adding/Updating a new item...");
      UpdateItemOutcome outcome = table.updateItem(updateItemSpec);
      System.out.println("Update Success! " + outcome);
    } catch (Exception e) {
      // add/update failed
      System.err.println("Unable to add/update item: " + skierId + " " + resortId + " " + dayId);
      System.err.println(e.getMessage());
    }
  }

  public void createLiftRideWithUuid(LiftRide newLiftRide) {
    int skierId = Integer.parseInt(newLiftRide.getSkierID());
    String resortId = newLiftRide.getResortID();
    int dayId = Integer.parseInt(newLiftRide.getDayID());
    int time = Integer.parseInt(newLiftRide.getTime());
    int liftId = Integer.parseInt(newLiftRide.getLiftID());
    String uuid = UuidCreator.getRandomBased().toString();

    try {
      // try to add created item
      System.out.println("Adding/Updating a new item...");
      PutItemOutcome outcome = table.putItem(
          new Item()
              .withPrimaryKey("uuid", uuid, "skierId", skierId)
              .withString("resortId", resortId)
              .withInt("liftId", liftId)
              .withInt("dayId", dayId)
              .withInt("time", time)
      );
      System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
    } catch (Exception e) {
      // add failed
      System.err.println("Unable to add/update item: " + skierId + " " + resortId + " " + dayId);
      System.err.println(e.getMessage());
    }
  }

  private static UpdateItemSpec updateItemSpec(int skierId, String resortId, int liftId, int dayId, int time) {
    Map<String, Object> map = new HashMap<>();
    map.put("liftId", liftId);
    map.put("time", time);
    map.put("dayId", dayId);
    return new UpdateItemSpec()
        .withPrimaryKey("skierId", skierId, "resortId", resortId)
        .withUpdateExpression("set records = list_append(if_not_exists(records, :il), :al)")
        .withValueMap(new ValueMap()
            .withList(":al", Arrays.asList(map))
            .withList(":il", Arrays.asList())
        )
        .withReturnValues(ReturnValue.ALL_NEW);
  }
}
