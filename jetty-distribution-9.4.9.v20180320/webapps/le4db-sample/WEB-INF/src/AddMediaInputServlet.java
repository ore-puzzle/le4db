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
public class AddMediaInputServlet extends HttpServlet {

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

		String status = (String)session.getAttribute("add_media_status");

		String errorMessage = "";
		String addStr = "";
		if(status != null) {
			switch(status) {
			case "reject_empty":
				errorMessage = "すべての情報を入力してください";
				break;
			case "reject_not_num":
				errorMessage = "出版年は数字で入力してください";
				break;
			case "reject_not_time":
				errorMessage = "長さが不適切です";
				break;
			case "accept":
				addStr = "<table border=\"1\"><th>mid</th><th>タイトル</th><th>出版年</th>\n"
                                 + "<tr><td>" + request.getParameter("mid") + "</td><td>" + request.getParameter("title")
                                 + "</td><td>" + request.getParameter("published_year") + "</td></tr></table>\n"
                                 + "を登録しました<br><br>";
			default:

			}
			session.removeAttribute("add_media_status");
		}

		

		out.println("<html>");
		out.println("<body>");
		
		out.println("<h3>メディア登録</h3>");

		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

		out.println(addStr);

		out.println("<form action=\"add_media\" method=\"GET\">");
		out.println("媒体");
		out.println("<input type=\"radio\" name=\"media\" value=\"Blu-ray\">Blu-ray</input>");
		out.println("<input type=\"radio\" name=\"media\" value=\"DVD\">DVD</input>");
		out.println("<input type=\"radio\" name=\"media\" value=\"VHS\">VHS</input>");
		out.println("<br>");
		out.println("タイトル: ");
		out.println("<input type=\"text\" name=\"title\"/>");
		out.println("<br>");
		out.println("出版年: ");
		out.println("<input type=\"text\" name=\"published_year\"/>");
		out.println("<br>");
		out.println("出版社: ");
		out.println("<input type=\"text\" name=\"publisher\"/>");
		out.println("<br>");
		out.println("長さ: ");
		out.println("<input type=\"text\" name=\"hour\"/>");
		out.println("時間");
		out.println("<input type=\"text\" name=\"minute\"/>");
		out.println("分");
		out.println("<input type=\"text\" name=\"second\"/>");
		out.println("秒");
		out.println("<br>");
		out.println("種類: ");
		out.println("<input type=\"radio\" name=\"genre\" value=\"movie\">映画</input>");
		out.println("<input type=\"radio\" name=\"genre\" value=\"drama\">ドラマ</input>");
		out.println("<input type=\"radio\" name=\"genre\" value=\"variety\">バラエティ</input>");
		out.println("<input type=\"radio\" name=\"genre\" value=\"anime\">アニメ</input>");
		out.println("<input type=\"radio\" name=\"genre\" value=\"sport\">スポーツ</input>");
		out.println("<input type=\"radio\" name=\"genre\" value=\"documentary\">ドキュメンタリー</input>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
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
