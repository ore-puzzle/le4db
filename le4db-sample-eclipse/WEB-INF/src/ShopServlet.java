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
public class ShopServlet extends HttpServlet {

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
		
		String shopName = request.getParameter("shopname");
		String shopAddress = request.getParameter("shopaddress");
		String filter = request.getParameter("filter");
		
		switch(filter) {
		case "no":
			filter = "";
			break;
		case "onlyrental":
			filter = " and finished = 'no'";
			break;
		default:
			filter = "";
		}

		out.println("<html>");
		out.println("<body>");

		out.println("<h3>" + shopName + "</h3>");
		out.println("<br>");
		out.println("貸出状況 ");
		out.println("<a href=\"add\">追加する</a>");
		out.println("<br>");
		
		out.println("<form action=\"shop\" method=\"GET\">");
		out.println("フィルター： ");
		out.println("<br>");
		out.println("<select name = \"filter\"");
		out.println("<option value=\"no\">なし</option>");
		out.println("<option value=\"onlyrental\">貸出中のみ</option>");
		out.println("</select>");
		out.println("</form>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.Driver");
			conn = DriverManager.getConnection("jdbc:sqlite://" + _dbname);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>メールアドレス</th>mid<th>貸出日</th><th>返却日</th><th>状態</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM rent WHERE mid = (SELECT mid FROM put WHERE shopname = '"
			                                 + shopName +  "' and shopaddress = '" + shopAddress + "'" + filter + ") ORDER BY return_date DESC");
			while (rs.next()) {
				String mailAddress = rs.getString("mailaddress");
				String mid = rs.getString("mid");
				String rentalDate = rs.getString("rental_date");
				String returnDate = rs.getString("return_date").replace('a', (char)0);
				String state = rs.getString("finished").equals("yes") ? "返却済み" : "貸出中";

				out.println("<tr>");
				out.println("<td>" + mailAddress + "</td>");
				out.println("<td>" + mid + "</td>");
				out.println("<td>" + rentalDate + "</td>");
				out.println("<td>" + returnDate + "</td>");
				out.println("<td>" + state + "</td>");
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
