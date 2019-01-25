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
		
		String searchTitle = request.getParameter("search_title");
		String searchGenre = request.getParameter("search_genre");

		if(searchTitle == null) {
			searchTitle = (String)session.getAttribute("search_title");
		} else {
			session.setAttribute("search_title", searchTitle);
		}
		if(searchGenre == null) {
			searchGenre = (String)session.getAttribute("search_genre");
		} else {
			session.setAttribute("search_genre", searchGenre);
		}

		String searchTitleStr = " and title LIKE '%" + searchTitle + "%'";
		String searchGenreStr = " and genre LIKE '%" + searchGenre + "%'";

		String shopName = request.getParameter("shopname");
		String shopAddress = request.getParameter("shopaddress");

		if(shopName == null) {
			shopName = (String)session.getAttribute("shopname");
		} else {
			session.setAttribute("shopname", shopName);
		}
		if(shopAddress == null) {
			shopAddress = (String)session.getAttribute("shopaddress");
		} else {
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
		String selectYear = "";
		String orderStr = "";
		switch(order) {
		case "alphabet":
			orderStr = "title ASC";
			selectAlphabet = "selected";
			break;
		case "length":
			orderStr = "length ASC";
			selectLength = "selected";
			break;
		case "published_year":
			orderStr = "published_year DESC";
			selectYear = "selected";
			break;
		default:
			orderStr = "title";
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
		out.println("<h3>" + (shopName.equals("") ? "メディア検索結果" : "メディア一覧") + "</h3>");
		if(shopName.equals("")) {
			out.println("検索したタイトル: " + searchTitle);
			out.println("<br>");
			out.println("検索したジャンル: " + translateGenre(searchGenre));
			out.println("<br><br>");
		}
		out.println("<form action=\"medialist_us\" method=\"GET\">");
		out.println("ソート: ");
		out.println("<select name =\"order\">");
		out.println("<option value=\"alphabet\" " + selectAlphabet + ">五十音順</option>");
		out.println("<option value=\"length\" " + selectLength + ">長さ順</option>");
		out.println("<option value=\"published_year\" " + selectYear + ">出版年順</option>");
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
			String putShop = "";
			if(!shopName.equals("")) {
				putShop = " and shopname = '" + shopName + "' and shopaddress = '" + shopAddress +"'";
			}
			out.println("<table border=\"1\">");
			out.println("<tr><th>タイトル</th><th>出版年</th><th>長さ</th><th>ジャンル</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT title, published_year, publisher, length, genre "
				                             + "FROM ((media NATURAL INNER JOIN store) NATURAL INNER JOIN put) NATURAL INNER JOIN content "
				                             + "WHERE 1 = 1" + searchTitleStr + searchGenreStr + putShop + "ORDER BY " + orderStr);
			while (rs.next()) {
				String title = rs.getString("title");
				int publishedYear = rs.getInt("published_year");
				String publisher = rs.getString("publisher");
				String length = rs.getString("length");
				String genre = rs.getString("genre");

				String genreJP = translateGenre(genre);

				out.println("<tr>");
				out.println("<td><a href=\"media?title=" + URLEncoder.encode(title, "UTF-8")
				            + "&published_year=" + publishedYear + "&publisher=" + URLEncoder.encode(publisher, "UTF-8")
				            + "&length=" + URLEncoder.encode(length, "UTF-8") + "&genre=" + URLEncoder.encode(genreJP, "UTF-8")
				            + "\">" + title + "</a></td>");
				out.println("<td>" + publishedYear + "</td>");
				out.println("<td>" + length + "</td>");
				out.println("<td>" + genreJP + "</td>");
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
		
		String formerURL = request.getParameter("formerURL");
		if(formerURL != null) {
			session.setAttribute("formerURL", formerURL);
		} else {
			formerURL = (String)session.getAttribute("formerURL");
		}
		
		out.println("<br>");
		out.println("<a href=\"" + formerURL + "\">前のページに戻る</a>");

		out.println("</body>");
		out.println("</html>");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
	
	private static String translateGenre(String genre) {
		switch(genre) {
			case "all":
				return "全て";
			case "movie":
				return "映画";
			case "drama":
				return "ドラマ";
			case "anime":
				return "アニメ";
			case "sport":
				return "スポーツ";
			case "documentary":
				return "ドキュメンタリー";
			case "variety":
				return "バラエティ";
			default:
				return "";
		}
	}
}
