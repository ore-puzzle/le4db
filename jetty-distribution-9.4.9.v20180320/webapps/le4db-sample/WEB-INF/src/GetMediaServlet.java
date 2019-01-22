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
public class GetMediaServlet extends HttpServlet {

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

		String mid = request.getParameter("mid");
		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");
		
		if(mid == null || mid.length() == 0) {
			session.setAttribute("get_media_status", "reject_empty");
			response.sendRedirect("/le4db-sample/get_media_input");
			return;
		}
		
		try {
			Integer.parseInt(mid);
		} catch(Exception e) {
			session.setAttribute("get_media_status", "reject_not_number");
			response.sendRedirect("/le4db-sample/get_media_input");
			return;
		}
		
		
		String title = "";
		int publishedYear = 0;
		String media = "";
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			int existsMedia = -1;
			ResultSet rs1 = stmt.executeQuery("SELECT count(*) AS num FROM media WHERE mid =" + mid);
			while (rs1.next()) {
				existsMedia = rs1.getInt("num");
			}
			if(existsMedia == 0) {
				session.setAttribute("get_media_status", "reject_not_found");
				response.sendRedirect("/le4db-sample/get_media_input");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			PreparedStatement st = conn.prepareStatement("INSERT INTO put VALUES(?, ?, ?)");
			st.setInt(1, Integer.parseInt(mid));
			st.setString(2, shopName);
			st.setString(3, shopAddress);
			st.executeUpdate();
			
			ResultSet rs2 = stmt.executeQuery("SELECT title, published_year, type "
			                                  + "FROM media NATURAL INNER JOIN store WHERE mid =" + mid);
			while(rs2.next()) {
				title = rs2.getString("title");
				publishedYear = rs2.getInt("published_year");
				media = rs2.getString("type");
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

		session.setAttribute("get_media_status", "accept");
		response.sendRedirect("/le4db-sample/get_media_input?mid=" + mid + "&title=" + URLEncoder.encode(title, "UTF-8")
		                      +"&published_year=" + publishedYear + "&media=" + URLEncoder.encode(media, "UTF-8"));

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
