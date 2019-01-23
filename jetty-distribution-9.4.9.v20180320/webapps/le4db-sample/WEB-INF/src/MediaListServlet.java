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
public class MediaListServlet extends HttpServlet {

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

		String selectMid = "";
		String selectAlphabet = "";
		String orderStr = "";
		switch(order) {
		case "id":
			orderStr = "mid";
			selectMid = "selected";
			break;
		case "alphabet":
			orderStr = "title";
			selectAlphabet = "selected";
			break;
		default:
			orderStr = "mid";
		}
		
		String removeMid = request.getParameter("mid");
		String removeTitle = request.getParameter("title");
		String removeYear = request.getParameter("published_year");
		String removeStr = "";
		if(removeMid != null) {
			removeStr = "<table border=\"1\"><tr><th>mid</th><th>タイトル</th><th>出版年</th></tr>\n"
				        + "<tr><td>" + removeMid + "</td><td>" + removeTitle + "</td><td>" + removeYear + "</td></tr></table>\n"
				        + "を削除しました<br><br>";
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName + "</h3>");
		out.println("<h3>メディア一覧</h3>");
		out.println(removeStr);
		out.println("<a href=\"get_media_input?shopname=" + shopName + "&shopaddress=" + shopAddress + "\">追加する</a><br><br>");
		out.println("<form action=\"medialist\" method=\"GET\">");
		out.println("ソート： ");
		out.println("<br>");
		out.println("<select name =\"order\">");
		out.println("<option value=\"id\" " + selectMid + ">mid順</option>");
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
			out.println("<tr><th>mid</th><th>タイトル</th><th>出版年</th><th>媒体</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT mid, title, published_year, type "
                                                         + "FROM (media NATURAL INNER JOIN store) NATURAL INNER JOIN put "
				                         + "WHERE shopname = '" + shopName + "' and shopaddress = '" + shopAddress
                                                         + "' ORDER BY " + orderStr + " ASC");
			while (rs.next()) {
				String mid = rs.getString("mid");
				String title = rs.getString("title");
				int publishedYear = rs.getInt("published_year");
				String type = rs.getString("type");

				out.println("<tr>");
				out.println("<td>" + mid + "</td>");
				out.println("<td>" + title + "</td>");
				out.println("<td>" + publishedYear + "</td>");
				out.println("<td>" + type + "</td>");
				out.println("<td><a href=\"remove_media?shopname=" + URLEncoder.encode(shopName, "UTF-8")
				            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "&mid=" + mid
				            + "&title=" + URLEncoder.encode(title, "UTF-8") + "&published_year=" + publishedYear
                                            + "\">削除</a></td>");
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
