import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
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
		int rentalYear = (Integer)session.getAttribute("rental_year");
		int rentalMonth = (Integer)session.getAttribute("rental_month");
		int rentalDay = (Integer)session.getAttribute("rental_day");
		String rentalDuration = request.getParameter("rental_duration");


		String rentalDate = rentalYear + "/" + rentalMonth + "/" + rentalDay;

		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(rentalYear, rentalMonth, rentalDay);
		calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(rentalDuration));

		String returnYear = String.valueOf(calendar.get(Calendar.YEAR));
		String returnMonth = String.valueOf(calendar.get(Calendar.MONTH));
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
			session.setAttribute("add_rental_status", "reject_not_num");
			response.sendRedirect("/le4db-sample/add_rental_input");
			return;
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
				existsMedia = rs2.getInt("num");		
			}
			if(existsUser == 0 || existsMedia == 0) {
				session.setAttribute("add_rental_status", "reject_not_found");
				response.sendRedirect("/le4db-sample/add_rental_input");
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			PreparedStatement st1 = conn.prepareStatement("INSERT INTO rent VALUES(?, ?, ?, ?, ?, 'no')");
			st1.setString(1, mailAddress);
			st1.setInt(2, Integer.parseInt(mid));
			st1.setInt(3, Integer.parseInt(fee));
			st1.setString(4, rentalDate);
			st1.setString(5, returnDate);
			st1.executeUpdate();

			PreparedStatement st2 = conn.prepareStatement("INSERT INTO duration VALUES(?, ?, ?)");
			st2.setString(1, rentalDate);
			st2.setString(2, rentalDuration);
			st2.setString(3, rentalDate);
			st2.executeUpdate();
			
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
		response.sendRedirect("/le4db-sample/shop?mail=" + mailAddress + "&mid=" + mid + 
                                      "&fee=" + fee + "&rental_date=" + rentalDate + "&return_date=" + returnDate);

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
