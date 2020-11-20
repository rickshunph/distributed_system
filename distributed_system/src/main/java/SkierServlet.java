import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    String urlPath = request.getPathInfo();
    BufferedReader reader = request.getReader();
    String json = readBigStringIn(reader);
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

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      response.setStatus(HttpServletResponse.SC_CREATED);
      // do any sophisticated processing with urlParts which contains all the url params
      // TODO: process url params in `urlParts`
      response.getWriter().write("It works! " + json);
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

    System.out.println(Arrays.toString(urlParts)); ////////
    System.out.println("isTotalVerticalSkiDayParams(urlParts) = "
        + isTotalVerticalSkiDayParams(urlParts)); ////////
    System.out.println("isTotalVerticalSkierParams(urlParts) = "
        + isTotalVerticalSkierParams(urlParts)); ////////
    if (isTotalVerticalSkiDayParams(urlParts)) {
      try {
//        int resortID = Integer.parseInt(urlParts[1]);
//        int dayID = Integer.parseInt(urlParts[3]);
//        int skierID = Integer.parseInt(urlParts[5]);
//        System.out.println("resortID: " + resortID + ", dayID: " + dayID
//            + ", skierID" + skierID); ///////

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\n"
            + "  \"resortID\": \"Mission Ridge isTotalVerticalSkiDay\",\n"
            + "  \"totalVert\": 56734\n"
            + "}");
      } catch (NumberFormatException nfe) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print("{\"message\": \"Parameters must be numbers\"}");
      }
    } else if (isTotalVerticalSkierParams(urlParts)) {
      try {
//        int resortID = Integer.parseInt(urlParts[1]);
        String resort = request.getParameter("resort");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\n"
            + "  \"resortID\": \"Mission Ridge isTotalVerticalSkier\",\n"
            + "  \"totalVert\": 56734\n"
            + "}");
      } catch (NumberFormatException nfe) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().print("{message: \"Parameters must be numbers\"}");
      }
    } else {
      Gson gson = new Gson();
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().print("{message: \"this URL not found\"}");
    }
  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    return true;
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
