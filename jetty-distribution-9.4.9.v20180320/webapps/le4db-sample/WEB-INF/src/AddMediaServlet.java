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
public class AddMediaServlet extends HttpServlet {

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

		String media = request.getParameter("media");
		String title = request.getParameter("title");
		String publishedYear = request.getParameter("published_year");
		String publisher = request.getParameter("publisher");
		String hour = request.getParameter("hour");
		String minute = request.getParameter("minute");
		String second = request.getParameter("second");
		String genre = request.getParameter("genre");
		
		if(media.length() == 0 || title.length() == 0 
		  || publishedYear.length() == 0 || publisher.length() == 0 
		  || hour.length() == 0 || minute.length() == 0 
		  || second.length() == 0|| genre.length() == 0) {
			session.setAttribute("add_media_status", "reject_empty");
			response.sendRedirect("/le4db-sample/add_media_input");
			return;
		}
		
		try {
			Integer.parseInt(publishedYear);
		} catch(Exception e) {
			session.setAttribute("add_media_status", "reject_not_num");
			response.sendRedirect("/le4db-sample/add_media_input");
			return;
		}
		
		int ihour = 0;
		int iminute = 0;
		int isecond = 0;
		try {
			ihour = Integer.parseInt(hour);
			iminute = Integer.parseInt(minute);
			isecond = Integer.parseInt(second);
		} catch(Exception e) {
			session.setAttribute("add_media_status", "reject_not_time");
			response.sendRedirect("/le4db-sample/add_media_input");
			return;
		}
		
		
		String length = "";
		if(ihour >= 0 && iminute >= 0 && iminute <= 59 && isecond >= 0 && isecond <= 59) {
			String shour = String.valueOf(ihour);
			String sminute = String.valueOf(iminute);
			String ssecond = String.valueOf(isecond);
			if(ihour < 10) {
				shour = "0" + shour;
			}
			if(iminute < 10) {
				sminute = "0" + sminute;
			}
			if(isecond < 10) {
				ssecond = "0" + ssecond;
			}
			
			length = shour + "時間" + sminute + "分" + ssecond + "秒";
		} else {
			session.setAttribute("add_media_status", "reject_not_time");
			response.sendRedirect("/le4db-sample/add_media_input");
			return;
		}
		
		int max_mid = -1;
		
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT max(mid) AS max_mid FROM media");
			while (rs.next()) {
				max_mid = rs.getInt("max_mid");
			}
			
			PreparedStatement st1 = conn.prepareStatement("INSERT INTO media VALUES(?, ?, ?)");
			st1.setInt(1, max_mid + 1);
			st1.setString(2, media);
			st1.setString(3, "no");
			st1.executeUpdate();
			
			PreparedStatement st2 = conn.prepareStatement("INSERT INTO content VALUES(?, ?, ?, ?, ?)");
			st2.setString(1, title);
			st2.setInt(2, Integer.parseInt(publishedYear));
			st2.setString(3, length);
			st2.setString(4, publisher);
			st2.setString(5, genre);
			st2.executeUpdate();
			
			PreparedStatement st3 = conn.prepareStatement("INSERT INTO store VALUES(?, ?, ?)");
			st3.setInt(1, max_mid + 1);
			st3.setString(2, title);
			st3.setInt(3, Integer.parseInt(publishedYear));
			st3.executeUpdate();
			
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

		session.setAttribute("add_media_status", "accept");
		response.sendRedirect("/le4db-sample/add_media_input?mid=" + (max_mid + 1) + "&title=" + URLEncoder.encode(title, "UTF-8")
		                      + "&published_year=" + publishedYear);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
