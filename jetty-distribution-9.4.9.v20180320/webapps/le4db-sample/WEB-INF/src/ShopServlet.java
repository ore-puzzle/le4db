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
public class ShopServlet extends HttpServlet {

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
		

		String searchMail = request.getParameter("search_mail");
		if(searchMail == null) {
			searchMail = (String)session.getAttribute("search_mail");
		} else {
			session.setAttribute("search_mail", searchMail);
		}

		String filter = request.getParameter("filter");
		if(filter == null) {
			filter = (String)session.getAttribute("filter");
		} else {
			session.setAttribute("filter", filter);
		}

		String usedStr = "";
		if(!searchMail.equals("")) {
			usedStr = "検索したメールアドレス: " + searchMail + "<br>";
		}
		String searchStr = " and mail LIKE '%" + searchMail + "%'";
		String valueStr = " value=\"" + searchMail + "\"";
		
		String selectNo = "";
		String selectRentalOnly = "";
		switch(filter) {
		case "no":
			filter = "";
			selectNo = "selected";
			break;
		case "rental_only":
			filter = " and finished = 'no'";
			selectRentalOnly = "selected";
			break;
		default:
			filter = "";
		}
		
		String status = (String)session.getAttribute("return_status");
		String errorMessage = "";
		String returnStr = "";
		if(status != null) {
			switch(status) {
				case "reject_error":
					errorMessage = "エラーが発生しました";
					break;
				case "accept": 
					returnStr = "<table border=\"1\"><tr><th>メールアドレス</th><th>mid</th><th>タイトル</th></tr>\n"
				       		+ "<tr><td>" + request.getParameter("mail") + "</td><td>" + request.getParameter("mid")
				       		+ "</td><td>" + request.getParameter("title") + "</td></tr></table>\n"
				        	+ "を返却しました<br><br>";
					break;
				default:
			}
			session.removeAttribute("return_status");
		}

		out.println("<html>");
		out.println("<body>");
		out.println("<h3>" + shopName + "</h3>");
		out.println("<h3>貸出状況</h3>");
		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");
		out.println(returnStr);
		out.println("<a href=\"add_rental_input\">追加する</a>");
		out.println("<br>");
		out.println(usedStr);
		if(!usedStr.equals("")) {
			out.println("<br>");
		}
		out.println("<form action=\"shop\" method=\"GET\">");
        out.println("メールアドレスで検索: ");
        out.println("<input type=\"text\" name=\"search_mail\"" + valueStr + "/>");
		out.println("<input type=\"submit\" value=\"検索\"/>");
        out.println("<br>");
		out.println("フィルター: ");
		out.println("<select name =\"filter\">");
		out.println("<option value=\"no\" " + selectNo + ">なし</option>");
		out.println("<option value=\"rental_only\" " + selectRentalOnly + ">貸出中のみ</option>");
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
			out.println("<tr><th>メールアドレス</th><th>mid</th><th>タイトル</th><th>貸出日</th><th>返却期限</th><th>状態</th></tr>");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM rent NATURAL INNER JOIN store WHERE mid = (SELECT mid FROM put WHERE shopname = '"
			                                 + shopName +  "' and shopaddress = '" + shopAddress + "'" + filter + ")" 
                                                         + searchStr + "ORDER BY return_date DESC");
			while (rs.next()) {
				String mailAddress = rs.getString("mail");
				String mid = rs.getString("mid");
                                String title = rs.getString("title");
				String rentalDate = rs.getString("rental_date");
				String returnDate = rs.getString("return_date");
				String state = rs.getString("finished").equals("yes") ? "返却済み" : "貸出中";

				out.println("<tr>");
				out.println("<td>" + mailAddress + "</td>");
				out.println("<td>" + mid + "</td>");
				out.println("<td>" + title + "</td>");
				out.println("<td>" + rentalDate + "</td>");
				out.println("<td>" + returnDate + "</td>");
				out.println("<td>" + state + "</td>");
				if(state.equals("返却済み")) {
					out.println("<td></td>");
				} else {
					out.println("<td><a href=\"return?mail=" + mailAddress + "&mid=" + mid
					             + "&title=" + URLEncoder.encode(title, "UTF-8") + "\">返却する</a></td>");
				}
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
		out.println("<a href=\"shoplist\">前のページに戻る</a>");

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
