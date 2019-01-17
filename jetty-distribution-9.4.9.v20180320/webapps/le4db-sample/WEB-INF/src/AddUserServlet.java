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
public class AddUserServlet extends HttpServlet {

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
		String userName = request.getParameter("username");
		String userAddress = request.getParameter("useraddress");
		String password = request.getParameter("password");

		if(!mailAddress.contains("@")) {
			session.setAttribute("add_user_status", "reject_not_address");
			response.sendRedirect("/le4db-sample/add_user_input");
			return;
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();
			
			int existsUser = -1;
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS num FROM user WHERE mail = '" + mailAddress + "'");
			while(rs.next()) {
				existsUser = rs.getInt("num");
			}
			if(existsUser > 0) {
				session.setAttribute("add_user_status", "reject_duplicate");
				response.sendRedirect("/le4db-sample/add_user_input");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}

			PreparedStatement st = conn.prepareStatement("INSERT INTO user VALUES(?, ?, ?, ?)");
			st.setString(1, mailAddress);
			st.setString(2, userName);
			st.setString(3, userAddress);
			st.setString(4, password);
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

		session.setAttribute("add_user_status", "accept");
		response.sendRedirect("/le4db-sample/add_user_input?mail=" + mailAddress
                                      + "&username=" + URLEncoder.encode(userName, "UTF-8")
                                      + "&useraddress=" + URLEncoder.encode(userAddress, "UTF-8"));

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
