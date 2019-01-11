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
public class AddRentalInputServlet extends HttpServlet {

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

		out.println("<html>");
		out.println("<body>");
		
		out.println("<form action=\"add_rental\" method=\"GET\">");
		out.println("メールアドレス: ");
		out.println("<input type=\"text\" name=\"mail\"/>");
		out.println("<br>");
		out.println("mid: ");
		out.println("<input type=\"text\" name=\"mid\"/>");
		out.println("<br>");
		out.println("料金: ");
		out.println("<input type=\"text\" name=\"fee\">");
		out.println("<br>");
		out.println("貸出年: ");
		out.println("<select name=\"rental_year\">");
		out.println("<option value=\"2019年\" selected>2019</option>");
		out.println("<option value=\"2020\">2020年</option>");
		out.println("</select>");
		out.println("貸出月: ");
		out.println("<select name=\"rental_month\">");
		out.println("<option value=\"01\" selected>1月</option>");
		out.println("<option value=\"02\">2月</option>");
		out.println("<option value=\"03\">3月</option>");
		out.println("<option value=\"04\">4月</option>");
		out.println("<option value=\"05\">5月</option>");
		out.println("<option value=\"06\">6月</option>");
		out.println("<option value=\"07\">7月</option>");
		out.println("<option value=\"08\">8月</option>");
		out.println("<option value=\"09\">9月</option>");
		out.println("<option value=\"10\">10月</option>");
		out.println("<option value=\"11\">11月</option>");
		out.println("<option value=\"12\">12月</option>");
		out.println("</select>");
		out.println("貸出日: ");
		out.println("<select name=\"rental_day\">");
		out.println("<option value=\"01\" selected>1日</option>");
		out.println("<option value=\"02\">2日</option>");
		out.println("<option value=\"03\">3日</option>");
		out.println("<option value=\"04\">4日</option>");
		out.println("<option value=\"05\">5日</option>");
		out.println("<option value=\"06\">6日</option>");
		out.println("<option value=\"07\">7日</option>");
		out.println("<option value=\"08\">8日</option>");
		out.println("<option value=\"09\">9日</option>");
		out.println("<option value=\"10\">10日</option>");
		out.println("<option value=\"11\">11日</option>");
		out.println("<option value=\"12\">12日</option>");
		out.println("<option value=\"13\">13日</option>");
		out.println("<option value=\"14\">14日</option>");
		out.println("<option value=\"15\">15日</option>");
		out.println("<option value=\"16\">16日</option>");
		out.println("<option value=\"17\">17日</option>");
		out.println("<option value=\"18\">18日</option>");
		out.println("<option value=\"19\">19日</option>");
		out.println("<option value=\"20\">20日</option>");
		out.println("<option value=\"21\">21日</option>");
		out.println("<option value=\"22\">22日</option>");
		out.println("<option value=\"23\">23日</option>");
		out.println("<option value=\"24\">24日</option>");
		out.println("<option value=\"25\">25日</option>");
		out.println("<option value=\"26\">26日</option>");
		out.println("<option value=\"27\">27日</option>");
		out.println("<option value=\"28\">28日</option>");
		out.println("<option value=\"29\">29日</option>");
		out.println("<option value=\"30\">30日</option>");
		out.println("<option value=\"31\">31日</option>");
		out.println("</select>");
		out.println("<br>");
		out.println("貸出期間: ");
		out.println("<select name=\"rental_duration\">");
		out.println("<option value=\"01\" selected>1日</option>");
		out.println("<option value=\"02\">2日</option>");
		out.println("<option value=\"03\">3日</option>");
		out.println("<option value=\"04\">4日</option>");
		out.println("<option value=\"05\">5日</option>");
		out.println("<option value=\"06\">6日</option>");
		out.println("<option value=\"07\">7日</option>");
		out.println("<option value=\"08\">8日</option>");
		out.println("<option value=\"09\">9日</option>");
		out.println("<option value=\"10\">10日</option>");
		out.println("<option value=\"11\">11日</option>");
		out.println("<option value=\"12\">12日</option>");
		out.println("<option value=\"13\">13日</option>");
		out.println("<option value=\"14\">14日</option>");
		out.println("</select>");
		out.println("<br>");
		out.println("<input type=\"submit\" value=\"追加\"/>");
		out.println("</form>");


		out.println("<br/>");
		out.println("<a href=\"shop\">前のページに戻る</a>");

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
