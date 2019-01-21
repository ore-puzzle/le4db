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
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddClerkServlet extends HttpServlet {

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

		String clerkName = request.getParameter("clerkname");
		String password = request.getParameter("password");
		
		if(clerkName == null || password == null || clerkName.length() == 0 || password.length() == 0) {
			session.setAttribute("add_clerk_status", "reject_empty");
			response.sendRedirect("/le4db-sample/add_clerk_input");
			return;
		}
		
		int max_eid = -1;
		
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT max(eid) AS max_eid FROM clerk");
			while (rs.next()) {
				max_eid = rs.getInt("max_eid");
			}
			
			PreparedStatement st = conn.prepareStatement("INSERT INTO clerk VALUES(?, ?, ?)");
			st.setInt(1, max_eid + 1);
			st.setString(2, clerkName);
			st.setString(3, password);
			st.executeUpdate();
			
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

		session.setAttribute("add_clerk_status", "accept");
		response.sendRedirect("/le4db-sample/add_clerk_input?eid=" + (max_eid + 1) + "&clerkname=" + URLEncoder.encode(clerkName, "UTF-8"));

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
