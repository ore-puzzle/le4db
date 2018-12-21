import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteRental {

    public static void main(String[] args) throws SQLException {
        String dbname = "rental.db"; // 利用するデータベースファイル
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);
            System.out.println("接続成功");

            stmt = conn.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT title FROM content WHERE genre = 'movie'");
            while (rs.next()) {
                String title = rs.getString("title");
                System.out.println(title);
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
