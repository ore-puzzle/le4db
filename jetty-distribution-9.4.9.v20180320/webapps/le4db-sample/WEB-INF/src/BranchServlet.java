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
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class BranchServlet extends HttpServlet {

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

		String identifier = request.getParameter("identifier");
		String password = request.getParameter("password");

                HttpSession session = request.getSession(true);
		
		int eid = -1;
		boolean isClerk = true;
		try {
			eid = Integer.parseInt(identifier);
		} catch(NumberFormatException e) {
			isClerk = false;
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			if(identifier.equals("supervisor")) {
				if(password.equals("svpw")) {
                   	session.setAttribute("identifier", "supervisor");
					response.sendRedirect("/le4db-sample/supervisor");
				} else {
					session.setAttribute("login_status", "reject");
					response.sendRedirect("/le4db-sample/login");
				}
			} else if (isClerk) {
				ResultSet rs = stmt.executeQuery("SELECT clerkpw FROM clerk WHERE eid = " + eid);
				if(rs.next() && password.equals(rs.getString("clerkpw"))) {
                  	session.setAttribute("identifier", eid);
					response.sendRedirect("/le4db-sample/clerk");
				} else {
					session.setAttribute("login_status", "reject");
					response.sendRedirect("/le4db-sample/login");
				}				
				rs.close();				
			} else {
				ResultSet rs = stmt.executeQuery("SELECT userpw FROM user WHERE mail = '" + identifier + "'");
				if(rs.next() && password.equals(rs.getString("userpw"))) {
					session.setAttribute("identifier", identifier);
					response.sendRedirect("/le4db-sample/user");
				} else {
					session.setAttribute("login_status", "reject");
					response.sendRedirect("/le4db-sample/login");
				}				
				rs.close();				
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
