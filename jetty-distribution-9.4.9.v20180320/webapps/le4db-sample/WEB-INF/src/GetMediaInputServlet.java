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
public class GetMediaInputServlet extends HttpServlet {

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

		String status = (String)session.getAttribute("get_media_status");

		String errorMessage = "";
		String addStr = "";
		if(status != null) {
			switch(status) {
			case "reject_empty":
				errorMessage = "midを入力してください";
				break;
			case "reject_not_number":
				errorMessage = "midは数字で入力してください";
				break;
			case "reject_not_found":
				errorMessage = "そのようなメディアは存在しません";
				break;
			case "reject_duplicate":
				errorMessage = "すでにこの店舗に置いてあります";
				break;
			case "reject_put_another_shop":
				errorMessage = "現在他の店に置かれています";
				break;
			case "reject_error":
				errorMessage = "エラーが発生しました";
				break;
			case "accept":
				addStr = "<table border=\"1\"><th>mid</th><th>タイトル</th><th>出版年</th><th>媒体</th>\n"
                                 + "<tr><td>" + request.getParameter("mid") + "</td><td>" + request.getParameter("title")
                                 + "</td><td>" + request.getParameter("published_year") + "</td><td>" + request.getParameter("media")
                                 + "</td></tr></table>\n"
                                 + "を登録しました<br><br>";
            	break;
			default:

			}
			session.removeAttribute("get_media_status");
		}

		

		out.println("<html>");
		out.println("<body>");
		
		out.println("<h3>メディア追加</h3>");

		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

		out.println(addStr);

		out.println("<form action=\"get_media\" method=\"GET\">");
		out.println("mid: ");
		out.println("<input type=\"text\" name=\"mid\"/>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
		out.println("<a href=\"medialist\">前のページに戻る</a>");

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
