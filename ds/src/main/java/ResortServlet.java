import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.client.model.TopTenTopTenSkiers;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResortServlet")
public class ResortServlet extends HttpServlet {
//  private Gson gson = new Gson();

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
//    response.setCharacterEncoding("UTF-8");
    String urlPath = request.getPathInfo();
    String resort = request.getParameter("resort");
    String dayID = request.getParameter("dayID");

//    JsonObject json = new Gson().toJson("{message: \"missing paramterers\"}");
//    PrintWriter out = response.getWriter();
//    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // check we have the two parameters!
    if (resort == null || dayID == null) {
//      Gson errorMessage = new Gson();
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
//      response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
//      PrintWriter out = response.getWriter();
      response.getWriter().print("{message: \"missing paramterers\"}");
//      out.print("{message: \"missing paramterers\"}");
//      out.flush();
      return;
    }

    response.setStatus(HttpServletResponse.SC_OK);
//    TopTenTopTenSkiers skiers = new TopTenTopTenSkiers().skierID("1").vertcialTotal(1200);
//    skiers.setSkierID("1");
//    skiers.setVertcialTotal(1200);
//    response.getWriter().write("{topTenVert: [" + "skiers" + "]}");
//    response.getWriter().write("{Resort: " + resort +
//        ", dayID: " + dayID + "}");
    response.getWriter().print("{\n"
        + "  \"topTenSkiers\": [\n"
        + "    {\n"
        + "      \"skierID\": 888899,\n"
        + "      \"VertcialTotal\": 30400\n"
        + "    }\n"
        + "  ]\n"
        + "}");
//    Gson gson = new Gson();
    response.getWriter().flush();
  }
}
