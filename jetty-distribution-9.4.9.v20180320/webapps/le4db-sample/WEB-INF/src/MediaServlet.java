import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MediaServlet extends HttpServlet {

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
		String title = (String)session.getAttribute("title");
		int publishedYear = (Integer)session.getAttribute("published_year");
		String publisher = (String)session.getAttribute("publisher");
		String length = (String)session.getAttribute("length");
		String genre = (String)session.getAttribute("genre");

		out.println("<html>");
		out.println("<body>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<h3>詳細</h3>");
			out.println("タイトル： " + title);
			out.println("<br>");
			out.println("出版年： " + publishedYear);
			out.println("<br>");
			out.println("出版社： " + publisher);
			out.println("<br>");
			out.println("長さ： " + length);
			out.println("<br>");
			out.println("ジャンル： " + genre);
			out.println("<br>");
			out.println("<table border=\"1\"><tr><th>置いてある店舗名</th><th>住所</th><th>媒体</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT shopname, shopaddress, type FROM (media NATURAL INNER JOIN put) NATURAL INNER JOIN store "
			                                 + "WHERE title = '" + title + "' and published_year = " + published_year);
			while (rs.next()) {
				String shopName = rs.getString("shopname");
				String shopAddress = rs.getString("shopaddress");
				String media = rs.getString("type");
				
				out.println("<tr><td>" + shopName + "</td>");
				out.println("<td>" + shopAddress + "</td>");
				out.println("<td>" + media + "</td></tr>");
			}
			
			out.println("</table>");
			
			rs.close();



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
		out.println("<a href=\"medialist_us\">前のページに戻る</a>");

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
