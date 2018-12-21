import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteSample1 {

    public static void main(String[] args) throws SQLException {
        String dbname = "sample.db"; // 利用するデータベースファイル
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbname);
            System.out.println("接続成功");

            stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE product (pid INTEGER, name VARCHAR(20), price INTEGER, PRIMARY KEY (pid))");
            System.out.println("テーブル作成");

            stmt.executeUpdate("INSERT INTO product VALUES(1, 'AAA', 100)");
            stmt.executeUpdate("INSERT INTO product VALUES(2, 'BBB', 80)");
            stmt.executeUpdate("INSERT INTO product VALUES(3, 'CCC', 220)");
            System.out.println("データ挿入");

            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM product WHERE price >= 100");
            System.out.println("選択");
            while (rs.next()) {
                int pid = rs.getInt("pid");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                System.out.println(pid + "\t" + name + "\t" + price);
            }
            rs.close();

            stmt.executeUpdate("DROP TABLE product");
            System.out.println("テーブル削除");
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
