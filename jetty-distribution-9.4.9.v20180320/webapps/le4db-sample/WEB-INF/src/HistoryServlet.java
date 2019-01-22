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
public class HistoryServlet extends HttpServlet {

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
		String userName = request.getParameter("username");

		out.println("<html>");
		out.println("<body>");

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<h3>" + userName + " さん</h3>");
			out.println("<h3>履歴</h3>");
			out.println("<table border=\"1\">");
			out.println("<tr><th>タイトル</th><th>貸出日</th><th>返却日</th><th></th></tr>");

			String title = "";
			String finished = "";
			String rentalDate = "";
			String returnDate = "";
			ResultSet rs = stmt.executeQuery("SELECT title, finished, rental_date, return_date FROM rent NATURAL INNER JOIN store"
                                                         + " WHERE mail = '" + mailAddress + "' ORDER BY rental_date DESC");
			while (rs.next()) {
				title = rs.getString("title");
				finished = rs.getString("finished");
				rentalDate = rs.getString("rental_date");
				returnDate = rs.getString("return_date");

				out.println("<tr>");
				
				out.println("<td>" + title + "</td>");
				out.println("<td>" + rentalDate + "</td>");
				out.println("<td>" + returnDate + "</td>");
				if(finished.equals("yes")) {
					out.println("<td>返却済み</td>");
				} else {
					out.println("<td>未返却</td>");
				}
				out.println("<tr>");
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
		out.println("<a href=\"user\">前のページに戻る</a>");

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
