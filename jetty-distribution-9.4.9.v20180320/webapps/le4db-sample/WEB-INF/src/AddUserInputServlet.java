import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
public class AddUserInputServlet extends HttpServlet {

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

		String status = (String)session.getAttribute("add_user_status");

		String errorMessage = "";
		String addStr = "";
		if(status != null) {
			switch(status) {
			case "reject_not_address":
				errorMessage = "メールアドレスが適切ではありません";
				break;
			case "reject_duplicate":
				errorMessage = "すでに登録されているメールアドレスです";
				break;
			case "accept":
				addStr = "<table border=\"1\"><th>メールアドレス</th><th>名前</th><th>住所</th>\n"
                                 + "<tr><td>" + request.getParameter("mail") + "</td><td>" + request.getParameter("username")
                                 + "</td><td>" + request.getParameter("useraddress") + "</td></tr></table>\n"
                                 + "を追加しました<br><br>";
			default:

			}
			session.removeAttribute("add_user_status");
		}

		

		out.println("<html>");
		out.println("<body>");
		
		out.println("<h3>ユーザ登録</h3>");

		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

		out.println(addStr);

		out.println("<form action=\"add_user\" method=\"GET\">");
		out.println("メールアドレス: ");
		out.println("<input type=\"text\" name=\"mail\"/>");
		out.println("<br>");
		out.println("名前: ");
		out.println("<input type=\"text\" name=\"username\"/>");
		out.println("<br>");
		out.println("住所: ");
		out.println("<input type=\"text\" name=\"useraddress\"/>");
		out.println("<br>");
		out.println("パスワード: ");
		out.println("<input type=\"password\" name=\"password\"/>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
		out.println("<a href=\"clerk\">前のページに戻る</a>");

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
