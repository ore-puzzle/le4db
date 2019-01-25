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
public class AddShopServlet extends HttpServlet {

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
		
		if(shopName.equals("") || shopAddress.equals("")) {
			session.setAttribute("add_shop_status", "reject_empty");
			response.sendRedirect("/le4db-sample/add_shop_input");
			return;
		}
		
		boolean successful = true;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			int existsShop = -1;
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS num FROM shop WHERE shopname ='" + shopName
			                                   + "' and shopaddress = '" + shopAddress + "'");
			while (rs.next()) {
				existsShop = rs.getInt("num");
			}
			if(existsShop > 0) {
				session.setAttribute("add_shop_status", "reject_duplicate");
				response.sendRedirect("/le4db-sample/add_shop_input");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			PreparedStatement st = conn.prepareStatement("INSERT INTO shop VALUES(?, ?, ?)");
			st.setString(1, shopName);
			st.setString(2, shopAddress);
			st.setInt(3, 0);
			st.executeUpdate();
			
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
			session.setAttribute("add_shop_status", "accept");
			response.sendRedirect("/le4db-sample/add_shop_input?shopname=" + URLEncoder.encode(shopName, "UTF-8")
		                      + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8"));
		} else {
			session.setAttribute("add_shop_status", "reject_error");
			response.sendRedirect("/le4db-sample/add_shop_input?shopname=" + URLEncoder.encode(shopName, "UTF-8")
		                      + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8"));
		}

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
