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
public class RemoveMediaServlet extends HttpServlet {

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
		int mid = Integer.parseInt(request.getParameter("mid"));
		String title = request.getParameter("title");
		int publishedYear = Integer.parseInt(request.getParameter("published_year"));

		boolean successful = true;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			
			PreparedStatement st1 = conn.prepareStatement("DELETE FROM put WHERE shopname = ? and shopaddress = ? and mid = ?");
			st1.setString(1, shopName);
			st1.setString(2, shopAddress);
			st1.setInt(3, mid);
			st1.executeUpdate();
			
			int totalMedia = -1;
			ResultSet rs = stmt.executeQuery("SELECT total_media FROM shop WHERE shopname = '" + shopName
				                               + "' and shopaddress = '" + shopAddress + "'");
			while(rs.next()) {
				totalMedia = rs.getInt("total_media");
			}
			
			PreparedStatement st2 = conn.prepareStatement("UPDATE shop SET total_media = ? WHERE shopname = ? and shopaddress = ?");
			st2.setInt(1, totalMedia - 1);
			st2.setString(2, shopName);
			st2.setString(3, shopAddress);
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
			session.setAttribute("remove_media_status", "accept");
			response.sendRedirect("/le4db-sample/medialist?mid=" + mid + "&title=" + URLEncoder.encode(title, "UTF-8")
			                   + "&published_year=" + publishedYear);
		} else {
			session.setAttribute("remove_media_status", "reject_error");
			response.sendRedirect("/le4db-sample/medialist?mid=" + mid + "&title=" + URLEncoder.encode(title, "UTF-8")
			                   + "&published_year=" + publishedYear);
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
