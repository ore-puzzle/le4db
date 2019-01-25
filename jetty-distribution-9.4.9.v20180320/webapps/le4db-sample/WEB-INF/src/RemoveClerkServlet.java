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
public class RemoveClerkServlet extends HttpServlet {

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

		String shopName = request.getParameter("shopname");
		String shopAddress = request.getParameter("shopaddress");
		int eid = Integer.parseInt(request.getParameter("eid"));
		String clerkName = request.getParameter("clerkname");

		boolean successful = true;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			
			PreparedStatement st1 = conn.prepareStatement("DELETE FROM work1 WHERE shopname = ? and shopaddress = ? and eid = ?");
			st1.setString(1, shopName);
			st1.setString(2, shopAddress);
			st1.setInt(3, eid);
			st1.executeUpdate();
			
			PreparedStatement st2 = conn.prepareStatement("DELETE FROM work2 WHERE shopname = ? and shopaddress = ? and eid = ?");
			st2.setString(1, shopName);
			st2.setString(2, shopAddress);
			st2.setInt(3, eid);
			st2.executeUpdate();
			
			int working = -1;
			ResultSet rs1 = stmt.executeQuery("SELECT count(*) AS num FROM work1 WHERE eid =" + eid);
			while(rs1.next()) {
				working = rs1.getInt("num");
			}
			if(working == 0) {
				PreparedStatement st3 = conn.prepareStatement("DELETE FROM clerk WHERE eid = ?");
				st3.setInt(1, eid);
				st3.executeUpdate();
			}
			
			int hasClerk = -1;
			ResultSet rs2 = stmt.executeQuery("SELECT count(*) AS num FROM work1 WHERE shopname ='" + shopName
			                                  + "' and shopaddress = '" + shopAddress + "'");
			while(rs2.next()) {
				hasClerk = rs2.getInt("num");
			}
			if(hasClerk == 0) {
				PreparedStatement st3 = conn.prepareStatement("DELETE FROM shop WHERE shopname = ? and shopaddress = ?");
				st3.setString(1, shopName);
				st3.setString(2, shopAddress);
				st3.executeUpdate();
			}
			
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
			session.setAttribute("remove_clerk_status", "accept");
			response.sendRedirect("/le4db-sample/clerklist?eid=" + eid + "&clerkname=" + URLEncoder.encode(clerkName, "UTF-8"));
		} else {
			session.setAttribute("remove_clerk_status", "reject_error");
			response.sendRedirect("/le4db-sample/clerklist?eid=" + eid + "&clerkname=" + URLEncoder.encode(clerkName, "UTF-8"));
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
