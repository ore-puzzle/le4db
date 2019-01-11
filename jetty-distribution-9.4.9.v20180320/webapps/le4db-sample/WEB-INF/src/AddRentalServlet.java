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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddRentalServlet extends HttpServlet {

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

		String mailAddress = request.getParameter("mail");
		String mid = request.getParameter("mid");
		String fee = request.getParameter("fee");
		String rentalYear = request.getParameter("rental_year");
		String rentalMonth = request.getParameter("rental_month");
		String rentalDay = request.getParameter("rental_day");
		String rentalDuration = request.getParameter("rantal_duration");


		String rentalDate = rentalYear + "/" + rentalMonth + "/" + rentalDay;

		Calendar calendar = Calendar.getInstance();
		try {
			calendar.clear();
			calendar.setLenient(false);
			calendar.set(Integer.parseInt(rentalYear), Integer.parseInt(rentalMonth) - 1, Integer.parseInt(rentalDay));
			calendar.setLenient(true);
			calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(rentalDuration));
		} catch(IllegalArgumentException e) {
			session.setAttribute("add_rental_status", "reject");
			response.sendRedirect("/le4db-sample/shop");
		}

		String returnYear = String.valueOf(calendar.get(Calendar.YEAR));
		String returnMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
		if(returnMonth.length() == 1) {
			returnMonth = "0" + returnMonth;
		}
		String returnDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		if(returnDay.length() == 1) {
			returnDay = "0" + returnDay;
		}

		String returnDate = returnYear + "/" + returnMonth + "/" + returnDay;

		try {
			Integer.parseInt(mid);
			Integer.parseInt(fee);
		} catch(NumberFormatException e) {
			session.setAttribute("add_rental_status", "reject");
			response.sendRedirect("/le4db-sample/shop");
		}

		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			stmt = conn.createStatement();

			int existsUser = -1;
			ResultSet rs1 = stmt.executeQuery("SELECT count(*) AS num FROM user WHERE mail ='" + mailAddress + "'");
			while (rs1.next()) {
				existsUser = rs1.getInt("num");
				
			}
			int existsMedia = -1;
			ResultSet rs2 = stmt.executeQuery("SELECT count(*) AS num FROM put WHERE mid =" + mid);
			while (rs2.next()) {
				existsUser = rs2.getInt("num");		
			}
			if(existsUser == 0 || existsMedia == 0) {
				session.setAttribute("add_rental_status", "reject");
				response.sendRedirect("/le4db-sample/shop");
			}
			
			stmt.executeUpdate("INSERT INTO rent VALUES('" + mailAddress + "', " + mid + ", " + fee + ", '" 
                                           + rentalDate + "', '" + returnDate + "', 'no')");

			stmt.executeUpdate("INSERT INTO duration VALUES('" + rentalDate + "', '"
				           + rentalDuration + "', '" + returnDate + ")");
			
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

		session.setAttribute("add_rental_status", "accept");
		response.sendRedirect("/le4db-sample/shop?mail=" + mailAddress + "&fee=" + fee + "&rental_date=" + rentalDate + "&return_date=" + returnDate);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
