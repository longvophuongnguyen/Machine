package MachineSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;

/*
 * How do you design a Coin-Operated Soda Machine?

You need to design a Machine which:

1. Accepts notes of 10.000, 20.000, 50.000, 100.000, 200.000 VND

2. Allows user to select products Coke (10.000), Pepsi (10.000), Soda (20.000)

3. Allows user to receive a refund by canceling the request.

4. Releases the selected product and remaining change if any

 */

public class Machine {
	private static final String GetWater = "select * from water ";

	public static void main(String[] args) {
		try {
			int total = 0;
			int tmp = 0;
			String idWater = "";
			int numWater = 0;
			Scanner scan = new Scanner(System.in);
			Connection conn = Connect();
			getDataAll(conn, GetWater);
			String input = null;
			// nhập tiền
			do {
				System.out.println("Input money: ");
				input = scan.nextLine();
				try {
					tmp = Integer.parseInt(input);
				} catch (Exception e) {
					input = "0";
				}
			} while (CheckMoney(conn, "SELECT * from money where name =" + input).equals(false));
			total = Integer.parseInt(input);
			System.out.println("Your money: " + total);
			// chọn nước
			int flags = 0;
			do {
				System.out.println("Which one do you choose ?: ");
				getDataWater(conn, GetWater + " where price <= " + total);
				input = scan.nextLine();
				try {
					tmp = Integer.parseInt(input);
				} catch (Exception e) {
					input = "0";
				}
				if (CheckIdWater(conn, GetWater + " where id =" + input + " and price <= " + total).equals(true)) {
					System.out.println("Do you want to change? 1 Yes, any : No ");
					// Đổi loại
					if (!scan.nextLine().equals("1")) {
						flags = 1;
					}
				}
			} while (flags == 0);
			flags = 0;
			idWater = input;
			// Chọn số lượng
			do {
				System.out.println("Amount ?: ");
				input = scan.nextLine();
				try {
					tmp = Integer.parseInt(input);
				} catch (Exception e) {
					input = "0";
				}
				if (total >= (Integer.parseInt(input) * GetPrice(conn, GetWater + " where id =" + idWater))) {
					flags = 1;
				} else {
					System.out.println("Maximun:" + total / GetPrice(conn, GetWater + " where id =" + idWater));
				}

			} while (flags == 0);
			numWater = Integer.parseInt(input);
			flags = 0;
			//Thanh toán hoặc hủy
			do {
				System.out.println("1 To Get water and pay change, 2 to Cancel transaction ");
				input = scan.nextLine();
				try {
					tmp = Integer.parseInt(input);
				} catch (Exception e) {
					input = "0";
				}
				if (input.equals("1")) {
					System.out.println("Get water and Money exchange:"
							+ (total - (GetPrice(conn, GetWater + " where id =" + idWater) * numWater)));
					UpdateAmonut(conn, " UPDATE WATER set qty=qty- " + numWater + " where id =" + idWater );
					flags = 1;
				}
				if (input.equals("2")) {
					System.out.println("Your Money: " + total);
					flags = 1;
				}
			} while (flags == 0);

			scan.close();
			System.out.println("See you again!");
		} catch (Exception ex) {
			System.out.println("Some thing err please call 0707.389.573 to help ! Message Error:" + ex.getMessage());
		}
	}

	private static Connection Connect() throws SQLException {
		SQLiteConfig config = new SQLiteConfig();
		config.enableFullSync(true);
		config.setReadOnly(false);
		SQLiteDataSource sQLiteDataSource = new SQLiteDataSource(config);
		String url = "jdbc:sqlite:" + "./Database/conn.sqlite";
		sQLiteDataSource.setUrl(url);
		Connection conn = sQLiteDataSource.getConnection();
		System.out.println("connect success " + conn);
		return conn;
	}

	private static Object CheckMoney(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		if (conn != null) {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				return true;
			}
			rs = stmt.executeQuery("SELECT * from money");
			System.out.println("Only accept");
			while (rs.next()) {
				System.out.print(rs.getString("name") + rs.getString("note") + "\t");
			}
			stmt.close();
			System.out.println("");
			return false;
		}
		return false;
	}

	private static Object getDataAll(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		if (conn != null) {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.println(rs.getString("name") + "(" + rs.getString("price") + " VND):" + rs.getString("qty"));
			}
			stmt.close();
		}
		return false;
	}

	private static Object getDataWater(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		if (conn != null) {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.println(
						rs.getString("id") + ".\t" + rs.getString("name") + "(" + rs.getString("price") + " VND):");
			}
			stmt.close();
		}
		return false;
	}

	private static Object CheckIdWater(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		if (conn != null) {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.println("Your choose: " + rs.getString("name") + "(" + rs.getString("price") + " VND):");
				stmt.close();
				return true;
			}
		}
		return false;
	}

	private static int GetPrice(Connection conn, String sql) throws SQLException {
		Statement stmt = null;
		int tmp = 0;
		if (conn != null) {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				tmp = Integer.parseInt(rs.getString("price"));
				stmt.close();
				return tmp;
			}
		}
		return 0;
	}

	private static Object UpdateAmonut(Connection conn, String sql) throws SQLException {
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			// update
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return true;
	}
}
