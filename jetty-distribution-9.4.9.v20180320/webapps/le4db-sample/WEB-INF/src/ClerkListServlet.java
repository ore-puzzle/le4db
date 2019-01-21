import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
public class ClerkListServlet extends HttpServlet {

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
		
		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");
		if(shopName == null) {
			shopName = request.getParameter("shopname");
			shopAddress = request.getParameter("shopaddress");
			session.setAttribute("shopname", shopName);
			session.setAttribute("shopaddress", shopAddress);
		}

		String order = request.getParameter("order");
		if(order == null) {
			order = (String)session.getAttribute("order");
		} else {
			session.setAttribute("order", order);
		}

		String selectEid = "";
		String selectAlphabet = "";
		switch(order) {
		case "eid":
			order = "eid";
			selectEid = "selected";
			break;
		case "alphabet":
			order = "clerkname";
			selectAlphabet = "selected";
			break;
		default:
			order = "eid";
		}
		
		String removeEid = request.getParameter("eid");
		String removeClerkName = request.getParameter("clerkname");
		String removeStr = "";
		if(removeEid != null) {
			removeStr = "<table border=\"1\"><tr><th>eid</th><th>名前</th></tr>\n"
				        + "<tr><td>" + removeEid + "</td><td>" + removeClerkName + "</td></tr></table>\n"
				        + "を削除しました<br><br>";
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName + "</h3>");
		out.println("<h3>店員一覧</h3>");
		out.println(removeStr);
		out.println("<a href=\"get_clerk_input?shopname=" + shopName + "&shopaddress=" + shopAddress + "\">追加する</a><br><br>");
		out.println("<form action=\"shop_sv\" method=\"GET\">");
		out.println("ソート： ");
		out.println("<br>");
		out.println("<select name =\"order\">");
		out.println("<option value=\"eid\" " + selectEid + ">eid順</option>");
		out.println("<option value=\"alphabet\" " + selectAlphabet + ">五十音順</option>");
		out.println("</select>");
		out.println("<input type=\"submit\" value=\"適用\"/>");
		out.println("</form>");


		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>eid</th><th>名前</th><th></th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT eid, clerkname FROM clerk NATURAL INNER JOIN work1 WHERE shopname = '" + shopName
			                                  + "' and shopaddress = '" + shopAddress + "' ORDER BY " + order + " ASC");
			while (rs.next()) {
				String eid = rs.getString("eid");
				String clerkName = rs.getString("clerkname");

				out.println("<tr>");
				out.println("<td>" + eid + "</td>");
				out.println("<td>" + clerkName + "</td>");
				out.println("<td><a href=\"remove_clerk?shopname=" + URLEncoder.encode(shopName, "UTF-8")
				            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "&eid=" + eid
				            + "&clerkname=" + URLEncoder.encode(clerkName, "UTF-8") + "\">削除</a></td>");
				out.println("</tr>");
			}
			rs.close();
			
			out.println("</table>");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		out.println("<br>");
		out.println("<a href=\"shop_sv\">前のページに戻る</a>");

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
