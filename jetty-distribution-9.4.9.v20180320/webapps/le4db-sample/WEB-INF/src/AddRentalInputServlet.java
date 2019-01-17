import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Calendar;
import java.util.Enumeration;

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

		String status = (String)session.getAttribute("add_rental_status");

		String errorMessage = "";
                if(status != null) {
                	switch(status) {
			case "reject_not_num":
				errorMessage = "midと料金は整数でなければなりません";
				break;
			case "reject_not_found":
				errorMessage = "そのようなユーザあるいはメディアは存在しません";
				break;
			default:

			}

                        session.removeAttribute("add_rental_status");
                }

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		session.setAttribute("rental_year", year);
		session.setAttribute("rental_month", month);
		session.setAttribute("rental_day", day);

		out.println("<html>");
		out.println("<body>");

		out.println("<h3>レンタル</h3>");
		
		out.println("<h4><font color=\"red\">" + errorMessage + "</font></h4>");

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
		out.println("貸出年月日: " + year + "年" + month + "月" + day + "日");
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


		out.println("<br>");
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
