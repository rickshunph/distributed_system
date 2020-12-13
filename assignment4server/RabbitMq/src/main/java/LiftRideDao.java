import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.sql.*;
import org.apache.commons.dbcp2.*;

public class LiftRideDao {
  private static BasicDataSource dataSource;

  public LiftRideDao() {
    dataSource = DBCPDataSource.getDataSource();
  }

  public void createLiftRide(LiftRide newLiftRide) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
//    String insertQueryStatement = "INSERT INTO Records_recordId (resortId, dayId, skierId, time, liftId) " +
//        "VALUES (?,?,?,?,?)";
    String insertQueryStatement = "INSERT INTO Records_uuid (uuid, resortId, dayId, skierId, time, liftId) " +
        "VALUES (UUID_TO_BIN(uuid()), ?,?,?,?,?)";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setInt(3, Integer.parseInt(newLiftRide.getSkierID()));
      preparedStatement.setString(1, newLiftRide.getResortID());
      preparedStatement.setInt(2, Integer.parseInt(newLiftRide.getDayID()));
      preparedStatement.setInt(4, Integer.parseInt(newLiftRide.getTime()));
      preparedStatement.setInt(5, Integer.parseInt(newLiftRide.getLiftID()));

      // execute insert SQL statement
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
  }

  // GET: get the total vertical for the skier for the specified ski day
  public SkierVertical getTotalVertAtDay(String resortId, int dayId, int skierId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "SELECT resortId AS resortID, sum(liftId) AS totalVert "
        + "FROM Records_recordId "
        + "WHERE resortId = ? AND skierId = ? AND dayId = ?";
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, resortId);
      preparedStatement.setInt(2, skierId);
      preparedStatement.setInt(3, dayId);

      // execute select SQL statement
      preparedStatement.execute();
      ResultSet resultSet = preparedStatement.getResultSet();
      SkierVertical skierVertical = new SkierVertical();
      if (resultSet != null && resultSet.next()) {
        int totalVert = resultSet.getInt(2);
        String resortID = resultSet.getString(1);
        resultSet.close();

        skierVertical.setResortID(resortID);
        skierVertical.setTotalVert(totalVert);

        return skierVertical;
      } else {
        resultSet.close();
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return null;
  }

  // GET: get the total vertical for the skier at a specified resort
  public SkierVertical getTotalVert(String resortId, int skierId) {
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    String insertQueryStatement = "SELECT resortId AS resortID, sum(liftId) AS totalVert "
        + "FROM Records_recordId "
        + "WHERE resortId = ? AND skierId = ?";
    System.out.println(insertQueryStatement + " " + resortId + " " + skierId);
    try {
      conn = dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement);
      preparedStatement.setString(1, resortId);
      preparedStatement.setInt(2, skierId);

      // execute select SQL statement
      preparedStatement.execute();
      ResultSet resultSet = preparedStatement.getResultSet();
      SkierVertical skierVertical = new SkierVertical();
      if (resultSet != null && resultSet.next()) {
        int totalVert = resultSet.getInt(2);
        String resortID = resultSet.getString(1);
        resultSet.close();

        skierVertical.setResortID(resortID);
        skierVertical.setTotalVert(totalVert);

        return skierVertical;
      } else {
        resultSet.close();
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
      }
    }
    return null;
  }
}