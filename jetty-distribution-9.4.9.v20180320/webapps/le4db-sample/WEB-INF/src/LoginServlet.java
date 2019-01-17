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
public class LoginServlet extends HttpServlet {
	
	public void init() throws ServletException {
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<body>");

                HttpSession session = request.getSession(true);

		out.println("<h3>ログイン</h3>");

                Object status = session.getAttribute("login_status");

                if(status != null) {
                	out.println("<h4><font color=\"red\">メールアドレスかパスワードが間違っています</font></h4>");

                        session.removeAttribute("login_status");
                }

		out.println("<form action=\"branch\" method=\"GET\">");
		out.println("メールアドレス： ");
		out.println("<input type=\"text\" name=\"identifier\"/>");
		out.println("<br/>");
		out.println("パスワード： ");
		out.println("<input type=\"password\" name=\"password\"/>");
		out.println("<br/>");
		out.println("<input type=\"submit\" value=\"ログイン\"/>");
		out.println("</form>");


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
