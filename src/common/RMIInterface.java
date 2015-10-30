package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIInterface extends Remote {
	

	public boolean login(String email, String password) throws RemoteException;
	

	public boolean register(String name,String email, String password) throws RemoteException;


	public boolean createProject(String name, String description, String date, String goal,String userEmail) throws RemoteException;


	public int checkBalance(String activeUser) throws RemoteException;


	public ArrayList<DatabaseRow> myProjectsList(String email) throws RemoteException;


	public ArrayList<DatabaseRow> projectLevelsList(Integer projectId) throws RemoteException;


	public boolean changeLevelGoal(int projectId, int levelId, int goal) throws RemoteException;


	public boolean addLevel(int projectId, int goal) throws RemoteException;

}
