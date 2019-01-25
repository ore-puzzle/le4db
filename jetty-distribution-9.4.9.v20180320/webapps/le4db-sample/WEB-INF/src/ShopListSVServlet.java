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
		
		session.setAttribute("shopname", "");
		session.setAttribute("shopaddress", "");
		
		String deleteShopName = request.getParameter("delete_shopname");
		String deleteShopAddress = request.getParameter("delete_shopaddress");
		String searchShopName = request.getParameter("search_shopname");
		String searchShopAddress = request.getParameter("search_shopaddress");
		
		if(searchShopName == null) {
			searchShopName = (String)session.getAttribute("search_shopname");
		} else {
			session.setAttribute("search_shopname", searchShopName);
		}
		if(searchShopAddress == null) {
			searchShopAddress = (String)session.getAttribute("search_shopaddress");
		} else {
			session.setAttribute("search_shopaddress", searchShopAddress);
		}
		
		String usedNameStr = searchShopName.equals("") ? "" : "検索した店舗名: " + searchShopName + "<br>";
		String searchNameStr = " and shopname LIKE '%" + searchShopName + "%'";
		String valueNameStr = " value=\"" + searchShopName + "\"";
		String usedAddressStr = searchShopAddress.equals("") ? "" : "検索した住所: " + searchShopAddress + "<br>";
		String searchAddressStr = " and shopaddress LIKE '%" + searchShopAddress + "%'";
		String valueAddressStr = " value=\"" + searchShopAddress + "\"";


		String deleteStr = "";
		if(deleteShopName != null) {
			deleteStr = "<table border=\"1\"><tr><th>店舗名</th><th>住所</th></tr>\n"
				        + "<tr><td>" + deleteShopName + "</td><td>" + deleteShopAddress + "</td></tr></table>\n"
				        + "を削除しました<br><br>";
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>店舗一覧</h3>");
		out.println(deleteStr);
		out.println("<a href=\"add_shop_input\">追加する</a><br><br>");
		out.println(usedNameStr);
		out.println(usedAddressStr);
		if(!usedNameStr.equals("") || !usedAddressStr.equals("")) {
			out.println("<br>");
		}
		out.println("<form action=\"shoplist_sv\" method=\"GET\">");
		out.println("店舗名で検索: ");
		out.println("<input type=\"text\" name=\"search_shopname\"" + valueNameStr + "/>");
		out.println("<br>");
		out.println("住所で検索: ");
		out.println("<input type=\"text\" name=\"search_shopaddress\"" + valueAddressStr + "/>");
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
			out.println("<tr><th>店舗名</th><th>住所</th><th></th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT shopname, shopaddress FROM shop WHERE 1 = 1"
                                                         + searchNameStr + searchAddressStr + " ORDER BY shopname ASC");
			while (rs.next()) {
				String shopName = rs.getString("shopname");
				String shopAddress = rs.getString("shopaddress");

				out.println("<tr>");
				out.println("<td><a href=\"shop_sv?shopname=" + URLEncoder.encode(shopName, "UTF-8")
                                            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "\">" + shopName + "</a></td>");
				out.println("<td>" + shopAddress + "</td>");
				out.println("<td><a href=\"delete_shop?shopname=" + URLEncoder.encode(shopName, "UTF-8")
				                            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "\">削除</a></td>");
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
