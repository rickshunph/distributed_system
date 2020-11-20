import com.google.gson.Gson;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {
  private static final Logger logger = LogManager.getLogger("DS_Server_Skier");
  private static final LiftRideDao liftRideDAO = new LiftRideDao();

  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    logger.info("Input: " + urlPath);

    BufferedReader reader = request.getReader();
    String json = readBigStringIn(reader);
//    System.out.println(json);
//    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("{message: \"missing parameters\"}");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    // parse url into the LiftRide object
    Gson gson = new Gson();
    LiftRide newLiftRide = gson.fromJson(json, LiftRide.class);

    // validate the LiftRide object
    if (!isUrlValid(newLiftRide)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().print("{\"message\": \"Missing Parameters\"}");
    } else {
      response.setStatus(HttpServletResponse.SC_CREATED);
      newLiftRide.setLiftID(Integer.parseInt(newLiftRide.getLiftID()) * 10 + "");
      liftRideDAO.createLiftRide(newLiftRide);
      response.getWriter().write(gson.toJson(json));
    }
  }

  public String readBigStringIn(BufferedReader buffIn) throws IOException {
    StringBuilder everything = new StringBuilder();
    String line;
    while( (line = buffIn.readLine()) != null) {
      everything.append(line);
    }
    return everything.toString();
  }

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (isTotalVerticalSkiDayParams(urlParts)) {
      try {
        String resort = urlParts[1];
        int dayId = Integer.parseInt(urlParts[3]);
        int skierId = Integer.parseInt(urlParts[5]);
//        System.out.println("resortID: " + resort + ", dayID: " + dayId
//            + ", skierID: " + skierId);

        SkierVertical result = liftRideDAO.getTotalVertAtDay(resort, dayId, skierId);

        if (result != null && result.getResortID() != null) {
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write("{\n"
              + "  \"resortID\": \"" + result.getResortID() + "\",\n"
              + "  \"totalVert\": " + result.getTotalVert() + "\n"
              + "}");
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.getWriter().write("{\n"
              + "  \"message\": Couldn't find record for resort ID = " + resort + ", "
              + "day ID = " + dayId + " and skier ID = " + skierId + ".\n"
              + "}");
        }
      } catch (NumberFormatException nfe) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print("{\"message\": \"Parameters must be numbers\"}");
      }
    } else if (isTotalVerticalSkierParams(urlParts)) {
      try {
        int skierId = Integer.parseInt(urlParts[1]);
        String resort = request.getParameter("resort");

        SkierVertical result = liftRideDAO.getTotalVert(resort, skierId);

        if (result != null && result.getResortID() != null) {
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write("{\n"
              + "  \"resortID\": \"" + result.getResortID() + "\",\n"
              + "  \"totalVert\": " + result.getTotalVert() + "\n"
              + "}");
        } else {
          response.setStatus(HttpServletResponse.SC_NOT_FOUND);
          response.getWriter().write("{\n"
              + "  \"message\": Couldn't find record for resort ID = " + resort + ", "
              + "and skier ID = " + skierId + ".\n"
              + "}");
        }
      } catch (NumberFormatException nfe) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print("{message: \"Parameters must be numbers\"}");
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().print("{message: \"this URL not found\"}");
    }
  }

  private boolean isUrlValid(LiftRide newLiftRide) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    return newLiftRide.getDayID() != null && newLiftRide.getLiftID() != null &&
        newLiftRide.getResortID() != null && newLiftRide.getSkierID() != null &&
        newLiftRide.getTime() != null;
  }

  private boolean isTotalVerticalSkiDayParams(String[] urlParts) {
    return urlParts.length == 6 &&
        urlParts[2].equalsIgnoreCase("days") &&
        urlParts[4].equalsIgnoreCase("skiers");
  }

  private boolean isTotalVerticalSkierParams(String[] urlParts) {
    return urlParts.length == 3 &&
        urlParts[2].equalsIgnoreCase("vertical");
  }
}
