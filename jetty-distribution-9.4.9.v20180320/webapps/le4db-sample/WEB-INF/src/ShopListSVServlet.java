import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ShopListSVServlet extends HttpServlet {

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
		
		String searchShopName = request.getParameter("shopname");
		String searchShopAddress = request.getParameter("shopaddress");

		String usedNameStr = "";
		String usedAddressStr = "";
		String searchNameStr = "";
		String searchAddressStr = "";
		String valueNameStr = "";
		String valueAddressStr = "";
		if(searchShopName != null && !searchShopName.equals("")) {
			usedNameStr = "検索した店名: " + searchShopName + "<br>";
			searchNameStr = " and shopname LIKE '%" + searchShopName + "%'";
			valueNameStr = " value=\"" + searchShopName + "\"";
		}
		if(searchShopAddress != null && !searchShopAddress.equals("")) {
			usedAddressStr = "検索した住所: " + searchShopAddress + "<br>";
			searchAddressStr = " and shopaddress LIKE '%" + searchShopAddress + "%'";
			valueAddressStr = " value=\"" + searchShopAddress + "\"";
		}


		out.println("<html>");
		out.println("<body>");
		out.println("<h3>店舗一覧</h3>");
		out.println(usedNameStr);
		out.println(usedAddressStr);
		
		if(usedNameStr.length() + usedAddressStr.length() != 0) {
			out.println("<br>");
		}

		out.println("<form action=\"shoplist_sv\" method=\"GET\">");
                out.println("店名で検索: ");
                out.println("<input type=\"text\" name=\"shopname\"" + valueNameStr + "/>");
		out.println("<br>");
		out.println("住所で検索: ");
                out.println("<input type=\"text\" name=\"shopaddress\"" + valueAddressStr + "/>");
		out.println("<input type=\"submit\" value=\"検索\"/>");
		out.println("</form>");
                out.println("<br>");


		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>店名</th><th>住所</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT shopname, shopaddress FROM shop WHERE 1 = 1"
                                                         + searchNameStr + searchAddressStr + " ORDER BY shopname DESC");
			while (rs.next()) {
				String shopName = rs.getString("shopname");
				String shopAddress = rs.getString("shopaddress");

				out.println("<tr>");
				out.println("<td><a href=\"shop_sv?shopname=" + URLEncoder.encode(shopName, "UTF-8")
                                            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "\">" + shopName + "</a></td>");
				out.println("<td>" + shopAddress + "</td>");
				out.println("</tr>");
			}
			rs.close();
			
			out.println("</table>");
			
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
		
		out.println("<br>");
		out.println("<a href=\"supervisor\">前のページに戻る</a>");

		out.println("</body>");
		out.println("</html>");
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
