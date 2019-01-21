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
public class GetClerkServlet extends HttpServlet {

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

		String eid = request.getParameter("eid");
		String[] commuteMethod = request.getParameterValues("commute_method");
		String goodPoint = request.getParameter("good_point");
		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");
		
		if(eid == null || eid.length() == 0 || commuteMethod == null || commuteMethod.length == 0
		                                                  || goodPoint == null || goodPoint.length() == 0) {
			session.setAttribute("get_clerk_status", "reject_empty");
			response.sendRedirect("/le4db-sample/get_clerk_input");
			return;
		}
		
		try {
			Integer.parseInt(eid);
		} catch(Exception e) {
			session.setAttribute("get_clerk_status", "reject_not_number");
			response.sendRedirect("/le4db-sample/get_clerk_input");
			return;
		}
		
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			int existsClerk = -1;
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS num FROM clerk WHERE eid =" + eid);
			while (rs.next()) {
				existsClerk = rs.getInt("num");
			}
			if(existsClerk == 0) {
				session.setAttribute("get_clerk_status", "reject_not_found");
				response.sendRedirect("/le4db-sample/get_clerk_input");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			
			for(int i = 0; i < commuteMethod.length; i++) {
				PreparedStatement st1 = conn.prepareStatement("INSERT INTO work1 VALUES(?, ?, ?, ?)");
				st1.setInt(1, Integer.parseInt(eid));
				st1.setString(2, shopName);
				st1.setString(3, shopAddress);
				st1.setString(4, commuteMethod[i]);
				st1.executeUpdate();
			}
			
			PreparedStatement st2 = conn.prepareStatement("INSERT INTO work2 VALUES(?, ?, ?, ?)");
			st2.setInt(1, Integer.parseInt(eid));
			st2.setString(2, shopName);
			st2.setString(3, shopAddress);
			st2.setString(4, goodPoint);
			st2.executeUpdate();
			
			for(int i = 0; i < commuteMethod.length; i++) {
				PreparedStatement st3 = conn.prepareStatement("INSERT INTO attached_info VALUES(?, ?)");
				st3.setString(1, commuteMethod[i]);
				st3.setString(2, goodPoint);
				st3.executeUpdate();
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

		session.setAttribute("get_clerk_status", "accept");
		response.sendRedirect("/le4db-sample/get_clerk_input?eid=" + eid);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
