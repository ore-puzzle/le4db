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
public class DeleteShopServlet extends HttpServlet {

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

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery("SELECT count(*) AS num FROM shop WHERE shopname = '" + shopName
			                                  + "' and shopaddress = '" + shopAddress + "'");
			int existsShop = -1;
			while(rs.next()) {
				existsShop = rs.getInt("num");
			}
			if(existsShop == 0) {
				session.setAttribute("delete_shop_status", "reject");
				response.sendRedirect("/le4db-sample/shoplist_sv");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
				e.printStackTrace();
				}
				return;
			}

			PreparedStatement st = conn.prepareStatement("DELETE FROM shop WHERE shopname = ? and shopaddress = ?");
			st.setString(1, shopName);
			st.setString(2, shopAddress);
			st.executeUpdate();
			
			conn.commit();

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
		
		response.sendRedirect("/le4db-sample/shoplist_sv?delete_shopname=" + URLEncoder.encode(shopName, "UTF-8")
				                  + "&delete_shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8"));
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}

}
