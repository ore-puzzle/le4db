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
public class UserServlet extends HttpServlet {

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
		String mailAddress = (String)session.getAttribute("identifier");

		session.setAttribute("search_shopname", "");
		session.setAttribute("search_shopaddress", "");
		session.setAttribute("order", "alphabet");

		out.println("<html>");
		out.println("<body>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			
			String userName = "";
			ResultSet rs = stmt.executeQuery("SELECT username FROM user WHERE mail = '" + mailAddress + "'");
			while (rs.next()) {
				userName = rs.getString("username");
			}
			rs.close();

			out.println("<h3>" + userName + " さん</h3>");
			out.println("<h4>店舗検索</h4>");
			out.println("<form action=\"shoplist_us\" method=\"GET\">");
			out.println("店舗名で検索: ");
			out.println("<input type=\"text\" name=\"search_shopname\"/>");
			out.println("<br>");
			out.println("住所で検索: ");
			out.println("<input type=\"text\" name=\"search_shopaddress\"/>");
			out.println("<input type=\"submit\" value=\"検索\"/>");
			out.println("</form>");
			out.println("<h4>作品検索</h4>");
			out.println("<form action=\"medialist_us\" method=\"GET\">");
			out.println("タイトルで検索: ");
			out.println("<input type=\"text\" name=\"search_title\"/>");
			out.println("<br>");
			out.println("ジャンルで検索: ");
			out.println("<input type=\"text\" name=\"search_shopaddress\"/>");
			out.println("<input type=\"hidden\" name=\"formerURL\" value=\"user\"/>");
			out.println("<input type=\"submit\" value=\"検索\"/>");
			out.println("</form>");
			out.println("<br><br>");
			out.println("<a href=\"history?username=" + userName + "\">履歴</a>");

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
