import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
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
public class ReturnServlet extends HttpServlet {

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

		String mailAddress = request.getParameter("mail");
		int mid = Integer.parseInt(request.getParameter("mid"));
		String title = request.getParameter("title");

		boolean successful = true;
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);

			PreparedStatement st1 = conn.prepareStatement("UPDATE rent SET finished = 'yes' WHERE mail = ? and mid = ?");
			st1.setString(1, mailAddress);
			st1.setInt(2, mid);
			st1.executeUpdate();
			
			PreparedStatement st2 = conn.prepareStatement("UPDATE media SET available = 'yes' WHERE mid = ?");
			st2.setInt(1, mid);
			st2.executeUpdate();
			
			conn.commit();

		} catch (Exception e) {
			e.printStackTrace();
			successful = false;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(successful) {
			session.setAttribute("return_status", "accept");
			response.sendRedirect("/le4db-sample/shop?mail=" + mailAddress + "&mid=" + mid + "&title=" + URLEncoder.encode(title, "UTF-8"));
		} else {
			session.setAttribute("return_status", "reject_error");
			response.sendRedirect("/le4db-sample/shop?mail=" + mailAddress + "&mid=" + mid + "&title=" + URLEncoder.encode(title, "UTF-8"));
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
