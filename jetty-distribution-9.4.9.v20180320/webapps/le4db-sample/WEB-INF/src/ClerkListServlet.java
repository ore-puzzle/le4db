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
public class ClerkListServlet extends HttpServlet {

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
		
		if(shopName == null) {
			shopName = (String)session.getAttribute("shopname");
		} else {
			session.setAttribute("shopname", shopName);
		}
		if(shopAddress == null) {
			shopAddress = (String)session.getAttribute("shopaddress");
		} else {
			session.setAttribute("shopaddress", shopAddress);
		}
		
		String searchClerkName = request.getParameter("search_clerkname");
		
		if(searchClerkName == null) {
			searchClerkName = (String)session.getAttribute("search_clerkname");
		} else {
			session.setAttribute("search_clerkname", searchClerkName);
		}
		
		String usedNameStr = searchClerkName.equals("") ? "" : "検索した名前: " + searchClerkName + "<br>";
		String searchNameStr = " and clerkname LIKE '%" + searchClerkName + "%'";
		String valueNameStr = " value=\"" + searchClerkName + "\"";

		String order = request.getParameter("order");
		if(order == null) {
			order = (String)session.getAttribute("order");
		} else {
			session.setAttribute("order", order);
		}
		
		String status = (String)session.getAttribute("remove_clerk_status");
		String errorMessage = "";
		String removeStr = "";
		if(status != null) {
			switch(status) {
				case "reject_error":
					errorMessage = "エラーが発生しました";
					break;
				case "accept":
					removeStr = "<table border=\"1\"><tr><th>eid</th><th>名前</th></tr>\n"
				        + "<tr><td>" + request.getParameter("eid") + "</td><td>" + request.getParameter("clerkname") + "</td></tr></table>\n"
				        + "を削除しました<br><br>";
					break;
				default:
				
			}
			session.removeAttribute("remove_clerk_status");
		}

		String selectEid = "";
		String selectAlphabet = "";
		String orderStr = "";
		switch(order) {
		case "id":
			orderStr = "eid";
			selectEid = "selected";
			break;
		case "alphabet":
			orderStr = "clerkname";
			selectAlphabet = "selected";
			break;
		default:
			orderStr = "eid";
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName + "</h3>");
		out.println("<h3>店員一覧</h3>");
		out.println(errorMessage);
		out.println(removeStr);
		out.println("<a href=\"get_clerk_input?shopname=" + URLEncoder.encode(shopName, "UTF-8")
		             + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "\">追加する</a><br><br>");
		out.println(usedNameStr);
		if(!usedNameStr.equals("")) {
			out.println("<br>");
		}
		out.println("<form action=\"clerklist\" method=\"GET\">");
		out.println("名前で検索: ");
		out.println("<input type=\"text\" name=\"search_clerkname\"" + valueNameStr + "/>");
		out.println("<input type=\"submit\" value=\"検索\"/>");
		out.println("</form>");
		out.println("<form action=\"clerklist\" method=\"GET\">");
		out.println("ソート: ");
		out.println("<select name =\"order\">");
		out.println("<option value=\"id\" " + selectEid + ">eid順</option>");
		out.println("<option value=\"alphabet\" " + selectAlphabet + ">名前順</option>");
		out.println("</select>");
		out.println("<input type=\"submit\" value=\"適用\"/>");
		out.println("</form>");


		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
                        String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			out.println("<table border=\"1\">");
			out.println("<tr><th>eid</th><th>名前</th><th></th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT DISTINCT eid, clerkname FROM clerk NATURAL INNER JOIN work1 WHERE shopname = '" + shopName
			                                  + "' and shopaddress = '" + shopAddress + "'" + searchNameStr + " ORDER BY " + orderStr + " ASC");
			while (rs.next()) {
				String eid = rs.getString("eid");
				String clerkName = rs.getString("clerkname");

				out.println("<tr>");
				out.println("<td>" + eid + "</td>");
				out.println("<td>" + clerkName + "</td>");
				out.println("<td><a href=\"remove_clerk?shopname=" + URLEncoder.encode(shopName, "UTF-8")
				            + "&shopaddress=" + URLEncoder.encode(shopAddress, "UTF-8") + "&eid=" + eid
				            + "&clerkname=" + URLEncoder.encode(clerkName, "UTF-8") + "\">削除</a></td>");
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
		out.println("<a href=\"shop_sv\">前のページに戻る</a>");

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
