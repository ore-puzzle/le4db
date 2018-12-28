import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class BranchServlet extends HttpServlet {

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

		String identifier = request.getParameter("identifier"");
		String password = request.getParameter("password");
		
		int eid;
		boolean isClerk = true;
		try {
			eid = Integer.parseInt(mailAddress);
		} catch(NumberFormatException e) {
			isClerk = false;
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.Driver");
			conn = DriverManager.getConnection("jdbc:sqlite://" + _dbname);
			stmt = conn.createStatement();

			if(mailAddress.equals("supervisor")) {
				if(password.equals("svpw")) {
					RequestDispatcher dispatch = request.getRequestDispatcher("/supervisor");
					dispatch.forward(request, response);
				} else {
					response.sendRedirect("/login");
				}
			} else if (isClerk) {
				ResultSet rs = stmt.executeQuery("SELECT password FROM work1 WHERE eid = " + eid);
				while (rs.next()) {
					String shopname = rs.getString("shopname");
				}
				rs.close();
			} else {
				
			}

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

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
