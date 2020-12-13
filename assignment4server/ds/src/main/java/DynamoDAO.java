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
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamoDAO {
  private AmazonDynamoDB client;
  private DynamoDB dynamoDB;
  private Table table;

  private static final Logger logger = LogManager.getLogger(DynamoDAO.class.getName());

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

  public SkierVertical getTotalVert(String resortId, int skierId) {
    HashMap<String, String> nameMap = new HashMap<String, String>();
    nameMap.put("#s", "skierId");
    nameMap.put("#r", "resortId");

    HashMap<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put(":skierId", skierId);
    valueMap.put(":resortId", resortId);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression("#s = :skierId and #r = :resortId")
        .withNameMap(nameMap)
        .withValueMap(valueMap);

    ItemCollection<QueryOutcome> items = null;
    Iterator<Item> iterator = null;
    Item item = null;

    items = table.query(querySpec);

    int totalVert = 0;
    iterator = items.iterator();
    while (iterator.hasNext()) {
      item = iterator.next();
      List<Map<String, Object>> list = (List<Map<String, Object>>) item.get("records");
      System.out.println(list);
      for (Map<String, Object> map : list) {
        int liftId = ((BigDecimal) map.get("liftId")).intValue();
        totalVert += liftId;
      }
    }

    SkierVertical skierVertical = new SkierVertical();
    skierVertical.setResortID(resortId);
    skierVertical.setTotalVert(totalVert);
    return skierVertical;
  }

  public SkierVertical getTotalVertAtDay(String resortId, int dayId, int skierId) {
    HashMap<String, String> nameMap = new HashMap<String, String>();
    nameMap.put("#s", "skierId");
    nameMap.put("#r", "resortId");

    HashMap<String, Object> valueMap = new HashMap<String, Object>();
    valueMap.put(":skierId", skierId);
    valueMap.put(":resortId", resortId);

    QuerySpec querySpec = new QuerySpec()
        .withKeyConditionExpression("#s = :skierId and #r = :resortId")
        .withNameMap(nameMap)
        .withValueMap(valueMap);

    ItemCollection<QueryOutcome> items = null;
    Iterator<Item> iterator = null;
    Item item = null;

    items = table.query(querySpec);

    int totalVert = 0;
    iterator = items.iterator();
    while (iterator.hasNext()) {
      item = iterator.next();
      List<Map<String, Object>> list = (List<Map<String, Object>>) item.get("records");
      System.out.println(list);
      for (Map<String, Object> map : list) {
        int liftId = ((BigDecimal) map.get("liftId")).intValue();
        int dayIdData = ((BigDecimal) map.get("dayId")).intValue();
        if (dayIdData == dayId) {
          totalVert += liftId;
        }
      }
    }

    SkierVertical skierVertical = new SkierVertical();
    skierVertical.setResortID(resortId);
    skierVertical.setTotalVert(totalVert);
    return skierVertical;
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

  public AmazonDynamoDB getClient() {
    return client;
  }

  public DynamoDB getDynamoDB() {
    return dynamoDB;
  }

  public Table getTable() {
    return table;
  }
}
