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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ClerkServlet extends HttpServlet {

	private String _hostname = null;
	private String _dbname = null;
	private String _username = null;
	private String _password = null;

	public void init() throws ServletException {
		// iniファイルから自分のデータベース情報を読み込む
		String iniFilePath = getServletConfig().getServletContext()
				.getRealPath("WEB-INF/le4db.ini");
		try {
			FileInputStream fis = new FileInputStream(iniFilePath);
			Properties prop = new Properties();
			prop.load(fis);
			_hostname = prop.getProperty("hostname");
			_dbname = prop.getProperty("dbname");
			_username = prop.getProperty("username");
			_password = prop.getProperty("password");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String eid = request.getParameter("eid");

		out.println("<html>");
		out.println("<body>");

		out.println("<h3>店舗一覧</h3>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.Driver");
			conn = DriverManager.getConnection("jdbc:sqlite://" + _dbname);
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT shopname, shopaddress FROM work1 WHERE eid = " + eid);
			while (rs.next()) {
				String shopName = rs.getString("shopname");
				String shopAddress = rs.getString("shopaddress")
				out.println("<a href=\"ShopServlet?shopname=" + shopName + "&shopaddress=" + shopAddress + "&filter=\"no\"" + "\">");
				out.println(shopName + " " + shopAddress);
				out.println("/a>");
				out.println("<br>");
			}
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
