import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GetClerkInputServlet extends HttpServlet {

	private String _dbname = null;

	public void init() throws ServletException {
		// iniファイルから自分のデータベース情報を読み込む
		String iniFilePath = getServletConfig().getServletContext()
				.getRealPath("WEB-INF/le4db.ini");
		try {
			FileInputStream fis = new FileInputStream(iniFilePath);
			Properties prop = new Properties();
			prop.load(fis);
			_dbname = prop.getProperty("dbname");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(true);

		String status = (String)session.getAttribute("get_clerk_status");

		String errorMessage = "";
		String addStr = "";
		if(status != null) {
			switch(status) {
			case "reject_empty":
				errorMessage = "すべての情報を入力してください";
				break;
			case "reject_not_number":
				errorMessage = "eidは数字で入力してください";
				break;
			case "reject_not_found":
				errorMessage = "そのような店員は存在しません";
				break;
			case "accept":
				addStr = "<table border=\"1\"><th>eid</th>\n"
                                 + "<tr><td>" + request.getParameter("eid")
                                 + "</td></tr></table>\n"
                                 + "を登録しました<br><br>";
			default:

			}
			session.removeAttribute("get_clerk_status");
		}

		

		out.println("<html>");
		out.println("<body>");
		
		out.println("<h3>店員追加</h3>");

		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

		out.println(addStr);

		out.println("<form action=\"get_clerk\" method=\"GET\">");
		out.println("eid: ");
		out.println("<input type=\"text\" name=\"eid\"/>");
		out.println("<br>");
		out.println("通勤手段: ");
		out.println("<input type=\"checkbox\" name=\"commute_method\" value=\"walk\">徒歩</input>");
		out.println("<input type=\"checkbox\" name=\"commute_method\" value=\"bicycle\">自転車</input>");
		out.println("<input type=\"checkbox\" name=\"commute_method\" value=\"car\">車</input>");
		out.println("<input type=\"checkbox\" name=\"commute_method\" value=\"bus\">バス</input>");
		out.println("<input type=\"checkbox\" name=\"commute_method\" value=\"train\">電車</input>");
		out.println("<br>");
		out.println("店の良いところ: ");
		out.println("<input type=\"text\" name=\"good_point\"/>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
		out.println("<a href=\"clerklist\">前のページに戻る</a>");

		out.println("</body>");
		out.println("</html>");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
