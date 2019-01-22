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
			session.setAttribute("search_title", searchTitle);
		}
		
		String searchGenre = (String)session.getAttribute("search_genre");
		if(searchGenre == null) {
			searchGenre = request.getParameter("search_genre");
			session.setAttribute("search_genre", searchGenre);
		}
		
		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");
		if(shopName == null) {
			shopName = request.getParameter("shopname");
			shopAddress = request.getParameter("shopaddress");
			session.setAttribute("shopname", shopName);
			session.setAttribute("shopaddress", shopAddress);
		}
		if(shopName == null) {
			shopName = "";
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
			selectLength = "selected";
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

			String searchTitleStr = "";
			if(searchTitle != null) {
				searchTitleStr = " and title LIKE '%" + searchTitle + "%'";
			}

			String searchGenreStr = "";
			if(searchGenre != null) {
				searchGenreStr = " and genre = '" + searchGenre + "'";
			}

			String available = "";
			String putShop = "";
			if(shopName != null && shopName.length() != 0) {
				available = "<th></th>";
				putShop = " and shopname = '" + shopName + "' and shopaddress = '" + shopAddress +"'";
			}
			out.println("<table border=\"1\">");
			out.println("<tr><th>タイトル</th><th>出版年</th><th>長さ</th><th>ジャンル</th><th>媒体</th>" + available + "</tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT title, published_year, publisher, length, genre, type "
				                             + "FROM ((media NATURAL INNER JOIN store) NATURAL INNER JOIN put) NATURAL INNER JOIN content "
				                             + "WHERE 1 = 1" + searchTitleStr + searchGenreStr + putShop + "ORDER BY " + order + " ASC");
			while (rs.next()) {
				String title = rs.getString("title");
				int publishedYear = rs.getInt("published_year");
				String publisher = rs.getString("publisher");
				String length = rs.getString("length");
				String genre = rs.getString("genre");
				String type = rs.getString("type");

				String genreJP = "";
				switch(genre) {
					case "movie":
						genreJP = "映画";
						break;
					case "drama":
						genreJP = "ドラマ";
						break;
					case "anime":
						genreJP = "アニメ";
						break;
					case "sport":
						genreJP = "スポーツ";
						break;
					case "documentary":
						genreJP = "ドキュメンタリー";
						break;
					case "variety":
						genreJP = "バラエティ";
						break;
					default:
						genreJP = genre;
				}

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
}
