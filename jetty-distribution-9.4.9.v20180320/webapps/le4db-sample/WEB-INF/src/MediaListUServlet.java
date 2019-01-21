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
public class MediaListUSServlet extends HttpServlet {

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
		
		String searchTitle = (String)session.getAttribute("search_title");
		if(searchTitle == null) {
			searchTitle = request.getParameter("search_title");
			session.setAttribute("search_title");
		}
		
		String searchGenre = request.getAttribute("search_genre");
		if(searchGenre == null) {
			searchGenre = request.getParameter("search_genre");
			session.setAttribute("search_genre");
		}
		
		String formerURL = (String)session.getAttribute("formerURL");
		if(formerURL == null) {
			formerURL = request.getParameter("formerURL");
			session.setAttribute("formerURL", formerURL);
		}
		
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

		String selectAlphabet = "";
		String selectLength = "";
		switch(order) {
		case "alphabet":
			order = "title";
			selectAlphabet = "selected";
			break;
		case "length":
			order = "length";
			selectMid = "selected";
			break;
		default:
			order = "title";
		}
		
		String filter = request.getParameter("filter");
		if(filter == null) {
			filter = (String)session.getAttribute("filter");
		} else {
			session.setAttribute("filter", filter);
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName + "</h3>");
		out.println("<h3>メディア一覧</h3>");
		out.println("<form action=\"medialist_us\" method=\"GET\">");
		out.println("ソート： ");
		out.println("<br>");
		out.println("<select name =\"order\">");
		out.println("<option value=\"alphabet\" " + selectAlphabet + ">五十音順</option>");
		out.println("<option value=\"length\" " + selectLength + ">長さ順</option>");
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

			String available = "";
			String selectStr = "";
			if(shopName != null) {
				available = "<th></th>";
				selectStr = " and shopname = '" + shopName + "' and shopaddress = '" + shopAddress +"'";
			}
			out.println("<table border=\"1\">");
			out.println("<tr><th>タイトル</th><th>出版年</th><th>長さ</th><th>媒体</th>" + available + "</tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT title, published_year, publisher, length, type "
				                             + "FROM (media NATURAL INNER JOIN store) NATURAL INNER JOIN put "
				                             + "WHERE 1 = 1" + selectStr + genre + "ORDER BY " + order + " ASC");
			while (rs.next()) {
				String title = rs.getString("title");
				int publishedYear = rs.getInt("published_year");
				String length = rs.getString("length");
				String type = rs.getString("type");

				out.println("<tr>");
				out.println("<td>" + title + "</td>");
				out.println("<td>" + publishedYear + "</td>");
				out.println("<td>" + length + "</td>");
				out.println("<td>" + genre + "</td>");
				out.println("<td><a href=\"media?title=" + URLEncoder.encode(title, "UTF-8")
				            + "&published_year=" + publishedYear + "&publisher=" + URLEncoder.encode(publisher, "UTF-8")
				            + "&length=" + URLEncoder.encode(length, "UTF-8") + "&genre=" + URLEncoder.encode(genre, "UTF-8")
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
