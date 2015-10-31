package rmi;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.sun.xml.internal.bind.v2.model.core.ID;

import common.DatabaseRow;
import javafx.scene.chart.PieChart.Data;

public class DatabaseConnection {

	private Connection connection;
	
	public DatabaseConnection()
	{
		try {
			System.out.println("Connecting to Database...");
			this.connection = getDatabaseConnection();
			System.out.println("Connected to Heroku Database");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
	
	public void closeConnection()
	{
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Database connection closed!");
	}

	private Connection getDatabaseConnection() throws URISyntaxException, SQLException {
		/*
		 * Database
		 * Username: fzekibiamscilk
		 * Password: 6zSggqjUA1eNrp9zNKT1hrlpdl
		 */
		URI dbUri = new URI("postgres://fzekibiamscilk:6zSggqjUA1eNrp9zNKT1hrlpdl@ec2-75-101-162-243.compute-1.amazonaws.com:5432/dujiun5jm8qrj");
		
	    String username = dbUri.getUserInfo().split(":")[0];
	    String password = dbUri.getUserInfo().split(":")[1];
	    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath()+"?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";
	    
	    return DriverManager.getConnection(dbUrl, username, password);
	}
	
	public boolean checkLogin(String email, String password)
	{
		String sqlQuery = null;
		ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			System.err.println("SQL: "+e);
		}
		sqlQuery =
				"select count(*) "
				+ "from user_account "
				+ "where email = '"+email+"' and password='"+password+"'";
		
		
		
		try {
			result = statement.executeQuery(sqlQuery);
			result.next();
			
			if(result.getInt(1)==1)
				return true;
			else
				return false;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean registerAccount(String name, String email, String password) {
		String sqlQuery = null;
		ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sqlQuery =
				"select count(*) "
				+ "from user_account "
				+ "where email = '"+email+"'";
		
		try {
			result = statement.executeQuery(sqlQuery);
			result.next();
			
			if(result.getInt(1)>0)
				return false;
			else
			{
				sqlQuery = "insert into user_account(user_name,email,password,balance) "
						+ "values ('"+name+"','"+email+"','"+password+"',100)";
				statement.executeUpdate(sqlQuery);
				return true;
			}

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
	}

	public boolean insertNewProject(String name, String description, String date, String goal, String userEmail) {
		String sqlQuery = null;
		//ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			statement.executeUpdate("set datestyle = 'ISO, DMY'");
			
			//Insert na tabela project
			sqlQuery = "insert into project (id_project,date_start,date_end,project_name,money_raised,project_description, is_open) "
				+ "values (nextval('project_id_seq'),current_timestamp,'"+date+"','"+name+"',0,'"+description+"',true)";
			statement.executeUpdate(sqlQuery);
			
			//insert na tabela manages
			sqlQuery = "insert into manages (email, id_project) "
					+ "values ('"+userEmail+"', currval('project_id_seq'))";
			statement.executeUpdate(sqlQuery);

			//insert na tabela level
			sqlQuery = "insert into level(id_project,level_id,objective) "
					+ "values (currval('project_id_seq'),0,"+goal+")";
			statement.executeUpdate(sqlQuery);
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public int checkBalance(String activeUser) {
		String sqlQuery = null;
		Statement statement = null;
		ResultSet result = null;
		int balance = -1;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "select balance "
					+ "from user_account "
					+ "where email = '"+activeUser+"'";
			
			result = statement.executeQuery(sqlQuery);
			
			result.next();

			balance = result.getInt(1);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return balance;
	}

	public ArrayList<DatabaseRow> getMyProjectsList(String email) 
	{
		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "SELECT p.id_project, p.project_name "
					+ "FROM user_account u, Project p, Manages m "
					+ "WHERE u.email = '"+email+"' and u.email = m.email "
					+ "and p.id_project = m.id_project and is_open = true";
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1)));
				rowInfo.add(resultSet.getString(2));
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public ArrayList<DatabaseRow> getProjectLevelsList(Integer projectId) {

		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "select level_id, objective "
					+ "from level "
					+ "where id_project = "+projectId;
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1)));
				rowInfo.add(Integer.toString(resultSet.getInt(2)));
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public boolean changeLevelGoal(int projectId, int levelId, int goal) {
		String sqlQuery = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			System.err.println("SQL: "+e);
		}
		sqlQuery = "update level "
				+ "set objective = "+goal+" "
				+ "where level_id = "+levelId+" and id_project = "+projectId+"";
		
		
		
		try {
			if(statement.executeUpdate(sqlQuery)>0)
				return true;
			else
				return false;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean addLevel(int projectId, int goal) {
		String sqlQuery = null;
		//ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			sqlQuery = "insert into level(id_project,level_id,objective) "
					+ "values ("+projectId+",nextval('level_id_seq'),"+goal+")";

			statement.executeUpdate(sqlQuery);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addReward(int projectId, int levelId, String description, int value) {
		String sqlQuery = null;
		//ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			sqlQuery = "insert into reward(reward_id,id_project,level_id,reward_description,value) "
					+ "values (nextval('reward_id_seq'),"+projectId+","+levelId+",'"+description+"', "+value+")";

			statement.executeUpdate(sqlQuery);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<DatabaseRow> getLevelRewardList(int projectId, int levelId) {

		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "select reward_id, reward_description, value "
					+ "from reward "
					+ "where id_project = "+projectId+" and level_id = "+levelId;
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1)));
				rowInfo.add(resultSet.getString(2));
				rowInfo.add(Integer.toString(resultSet.getInt(3)));
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public boolean removeReward(int rewardId) {
		String sqlQuery = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			sqlQuery = "delete from reward "
					+ "where reward_id = "+rewardId;

			statement.executeUpdate(sqlQuery);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean removeLevel(int levelId) {
		String sqlQuery = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			

			sqlQuery = "delete from reward "
					+ "where level_id = "+levelId;

			statement.executeUpdate(sqlQuery);

			sqlQuery = "delete from level "
					+ "where level_id = "+levelId;

			statement.executeUpdate(sqlQuery);
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean addAdministrator(int projectId, String email) {
		String sqlQuery = null;
		//ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			sqlQuery = "insert into manages(id_project,email) "
					+ "values ("+projectId+",'"+email+"')";

			statement.executeUpdate(sqlQuery);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<DatabaseRow> currentProjectsList() {
		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "SELECT p.id_project,p.date_end, p.project_name, p.money_raised, l.objective, p.project_description "
					+ "from project p, level l "
					+ "where l.id_project = p.id_project and l.level_id=0 and p.date_end > current_timestamp";
			
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1))); 	//project id
				rowInfo.add(resultSet.getString(2));					//date_end
				rowInfo.add(resultSet.getString(3));					//project name
				rowInfo.add(Integer.toString(resultSet.getInt(4)));		//money raise
				rowInfo.add(Integer.toString(resultSet.getInt(5)));		//project objective
				rowInfo.add(resultSet.getString(6));		//project description
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public ArrayList<DatabaseRow> pastProjectsList() {
		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "SELECT p.id_project,p.date_end, p.project_name, p.money_raised, l.objective, p.project_description "
					+ "from project p, level l "
					+ "where l.id_project = p.id_project and l.level_id=0 and p.date_end <= current_timestamp";
			
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1))); 	//project id
				rowInfo.add(resultSet.getString(2));					//date_end
				rowInfo.add(resultSet.getString(3));					//project name
				rowInfo.add(Integer.toString(resultSet.getInt(4)));		//money raise
				rowInfo.add(Integer.toString(resultSet.getInt(5)));		//project objective
				rowInfo.add(resultSet.getString(6));		//project description
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public ArrayList<DatabaseRow> activeRewardsList(int projectId) {
		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "select reward_id, reward_description, value "
					+ "from reward "
					+ "where id_project = "+projectId+" "
					+ "and level_id in (select level_id from level "
									+ "where id_project = "+projectId+" "
									+ "and objective <= (select money_raised "
									+ "from project "
									+ "where id_project = "+projectId+"))";
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(Integer.toString(resultSet.getInt(1)));	//reward_id
				rowInfo.add(resultSet.getString(2));				//reward description
				rowInfo.add(Integer.toString(resultSet.getInt(3)));	//reward value
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public boolean buyReward(int rewardId, String email) {
		String sqlQuery = null;
		//ResultSet result = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {

			sqlQuery = "insert into pledge(pledge_id,email_buyer, reward_id, email_receiver) "
					+ "values (nextval('pledge_id_seq'),'"+email+"',"+rewardId+",'"+email+"')";

			statement.executeUpdate(sqlQuery);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<DatabaseRow> getMyRewards(String activeUser) {
		ArrayList<DatabaseRow> table = new ArrayList<>();
		DatabaseRow row = null;
		ArrayList<String> rowInfo = null;
		String sqlQuery = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connection.createStatement();
			
			sqlQuery = "select r.reward_description, pr.project_name, pr.date_end > current_timestamp, r.value, p.pledge_id "
					+ "from pledge p, reward r, project pr "
					+ "where email_receiver = '"+activeUser+"' and p.reward_id = r.reward_id and pr.id_project = r.id_project;";
			
			
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				//esta parte está um bocado confusa, mas funciona
				rowInfo = new ArrayList<>();
				rowInfo.add(resultSet.getString(1)); 	//reward_description
				rowInfo.add(resultSet.getString(2));	//project_name
				rowInfo.add(Integer.toString(resultSet.getInt(4)));		//value
				rowInfo.add(resultSet.getString(3));	//project active?
				rowInfo.add(resultSet.getString(5));	//pledge Id
				row = new DatabaseRow(rowInfo);
				table.add(row);
			}
			
			
			for(int i=0;i<table.size();i++)
			{
				System.out.println(table.get(i).getColumns().toString());
				
			}
			
			System.out.println();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return table;
	}

	public boolean giveawayReward(int pledgeId, String emailReceiver) {
		String sqlQuery = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			System.err.println("SQL: "+e);
		}
		sqlQuery = "update pledge "
				+ "set email_receiver = '"+emailReceiver+"' "
				+ "where pledge_id = "+pledgeId;
		
		
		
		try {
			if(statement.executeUpdate(sqlQuery)>0)
				return true;
			else
				return false;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	public boolean cancelProject(int projectId) {
		String deleteProject = null;
		String deleteLevels = null;
		String deleteRewards = null;
		String deletePledges = null;
		String deleteMessages = null;
		String moneyBack = null;
		String deleteManages = null;
		Statement statement = null;
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			deleteProject = "delete from project "
					+ "where id_project =" +projectId;
			deleteManages = "delete from manages "
					+ "where id_project =" +projectId;
			deleteLevels = "delete from level "
					+ "where id_project ="+projectId;
			deleteRewards = "delete from reward "
					+ "where id_project ="+projectId;
			deleteMessages = "delete from message "
					+ "where id_project ="+projectId;
			deletePledges = "delete from pledge "
					+ "where reward_id in (select reward_id "
										+ "from reward "
										+ "where id_project = "+projectId+")";
			moneyBack = "update user_account "
					+ "set balance = (balance + aux.value) "
					+ "from (select sum(r.value) as value, p.email_buyer "
							+ "from pledge p, reward r "
							+ "where p.reward_id in (select reward_id "
												+ "from reward "
												+ "where id_project = "+projectId+") "
												+ "and p.reward_id = r.reward_id "
												+ "group by p.email_buyer) aux "
					+ "where email = aux.email_buyer";
			
			connection.setAutoCommit(false);
			
			statement.executeUpdate(moneyBack);
			statement.executeUpdate(deletePledges);
			statement.executeUpdate(deleteRewards);
			statement.executeUpdate(deleteMessages);
			statement.executeUpdate(deleteLevels);
			statement.executeUpdate(deleteManages);
			statement.executeUpdate(deleteProject);
			
			connection.commit();
			
			/*
			 * "delete from reward "
			+ "where level_id = "+levelId;
			 */
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("SQL Exception: "+e);
			if(connection != null)
			{
				System.err.println("Rolling back transaction");
				try {
					connection.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	//Rascunho
	/*
	Statement stm = dbCon.getConnection().createStatement();
	String sqlQuery = "select * from user_account";
	
	ResultSet res = stm.executeQuery(sqlQuery);
	res.next();
	System.out.println(res.getString("email"));
	*/
}
