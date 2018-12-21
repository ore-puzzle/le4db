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
public class DeleteServlet extends HttpServlet {

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

		String deletePID = request.getParameter("delete_pid");

		out.println("<html>");
		out.println("<body>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.Driver");
			conn = DriverManager.getConnection("jdbc:sqlite://" + _dbname);
			stmt = conn.createStatement();
			
			out.println("以下の商品を削除しました。<br/><br/>");
			out.println("商品ID: " + deletePID + "<br/>");

			ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE pid = " + deletePID);
			while (rs.next()) {
				String name = rs.getString("name");
				int price = rs.getInt("price");
				
				out.println("商品名: " + name + "<br/>");
				out.println("価格: " + price + "<br/>");
			}
			rs.close();

			stmt.executeUpdate("DELETE FROM products WHERE pid=" + deletePID);
			
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

		out.println("<br/>");
		out.println("<a href=\"list\">トップページに戻る</a>");

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
