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

		String shopName = (String)session.getAttribute("shopname");
		String shopAddress = (String)session.getAttribute("shopaddress");

		String mailAddress = request.getParameter("mail");
		String mid = request.getParameter("mid");
		String fee = request.getParameter("fee");
		int rentalYear = (Integer)session.getAttribute("rental_year");
		int rentalMonth = (Integer)session.getAttribute("rental_month");
		int rentalDay = (Integer)session.getAttribute("rental_day");
		String rentalDuration = request.getParameter("rental_duration");


		String rentalMonthStr = String.valueOf(rentalMonth);
		String rentalDayStr = String.valueOf(rentalDay);
		if(rentalMonthStr.length() == 1) {
			rentalMonthStr = "0" + rentalMonthStr;
		}
		if(rentalDayStr.length() == 1) {
			rentalDayStr = "0" + rentalDayStr;
		}
		String rentalDate = rentalYear + "/" + rentalMonthStr + "/" + rentalDayStr;

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

		boolean successful = true;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbfile = getServletContext().getRealPath("WEB-INF/" + _dbname);
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			conn.setAutoCommit(false);
			stmt = conn.createStatement();

			int existsUser = -1;
			ResultSet rs1 = stmt.executeQuery("SELECT count(*) AS num FROM user WHERE mail ='" + mailAddress + "'");
			while (rs1.next()) {
				existsUser = rs1.getInt("num");
				
			}
			int existsMedia = -1;
			ResultSet rs2 = stmt.executeQuery("SELECT count(*) AS num FROM put WHERE mid = " + mid 
                                                          + " and shopname = '" + shopName + "' and shopaddress = '" + shopAddress + "'");
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
			
			boolean available = true;
			ResultSet rs3 = stmt.executeQuery("SELECT available FROM media WHERE mid = " + mid);
			while (rs3.next()) {
				switch(rs3.getString("available")) {
					case "yes":
						available = true;
						break;
					case "no":
						available = false;
						break;
				}	
			}
			if(!available) {
				session.setAttribute("add_rental_status", "reject_not_available");
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
			
			int existsDuration = -1;
			ResultSet rs4 = stmt.executeQuery("SELECT count(*) AS num FROM duration WHERE rental_date = '" + rentalDate
                                               + "' and rental_duration = '" + (rentalDuration + "日") + "'");
			while (rs4.next()) {
				existsDuration = rs4.getInt("num");		
			}
			if(existsDuration == 0) {
				PreparedStatement st2 = conn.prepareStatement("INSERT INTO duration VALUES(?, ?, ?)");
				st2.setString(1, rentalDate);
				st2.setString(2, rentalDuration + "日");
				st2.setString(3, rentalDate);
				st2.executeUpdate();
			}
			
			PreparedStatement st3 = conn.prepareStatement("UPDATE media SET available = 'no' WHERE mid = ?");
			st3.setInt(1, Integer.parseInt(mid));
			st3.executeUpdate();
			
			conn.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			successful = false;
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if(successful) {
			session.setAttribute("add_rental_status", "accept");
			response.sendRedirect("/le4db-sample/add_rental_input?mail=" + mailAddress + "&mid=" + mid + 
                                      "&fee=" + fee + "&rental_date=" + rentalDate + "&return_date=" + returnDate);
        } else {
        	session.setAttribute("add_rental_status", "reject_error");
			response.sendRedirect("/le4db-sample/add_rental_input?mail=" + mailAddress + "&mid=" + mid + 
                                      "&fee=" + fee + "&rental_date=" + rentalDate + "&return_date=" + returnDate);
        }

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
	}
}
