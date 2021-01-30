package serveur;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.postgresql.util.PSQLException;





public class MainServer {
	private static int port = 50005;
	private static Socket s;
	private static ServerSocket ss;
	private static int essaie = 0;
	private static Connection bd;
	private static boolean id = false;
	private static BufferedReader flux_entree;
	private static String flux_entree_lu;
	private static PrintWriter pr;
	static String[] str;
	private static FileWriter Log;
	
	public static boolean sortie= true;
	//chemin du fichier log a changer en fonction des appareil 
	private static String filename="./log.txt";
	

	@SuppressWarnings("null")
	public static void main(String[] args) throws IOException, SQLException {

		id = false;

		ss = initSocktetS(port);
		if(sortie == true){
			bd = initConnexionBD();
			id = verificationID();
		
		}
		
	
		
			
		
		while (sortie) {

			if (id == false) {

				if (essaie <=2) {
					if (s.isConnected()) {
						s.close();
						try {
							Log.close();
						} catch (Exception e) {
							// TODO: handle exception
						}
					
					}
					id = verificationID();
				} else {
					if(essaie>2){
						essaie = 0;
					}
					
				}
			}

			if (flux_entree_lu == null && s.isConnected()) {
				errClient();
			}
			
			if (s.isClosed()) {
				id = false;
				s.close();
				Log.close();
			}
			
			
			if (id == true && s.isConnected()) {
				try {
					flux_entree_lu = flux_entree.readLine();
			
				}catch (SocketTimeoutException e) {
						id = false;
						s.close();
					   System.out.println("\nLe client a pris trop de temps à repondre le serveur s'est couper");
				}
				catch (Exception e) {
					id = false;
					s.close();
					
				}
				
				
				if (flux_entree_lu != null && s.isConnected() && id == true) {
					String[] entreSplit = null;
					if(!flux_entree_lu.isEmpty()) {
						entreSplit =flux_entree_lu.split(" ") ;
					}else {
						entreSplit[0]="";
					}
					
					System.out.println("L'entrée :" +entreSplit[0]);
					switch (entreSplit[0]) {
					case ("help"):
						System.out.println("reponse envoyer : Lises des commande accepter");
						sendMessage("-----? HELP ?-----", pr);
						sendMessage("vous pouvez taper ces commande selon vos besoin:", pr);
						sendMessage("1-down : vous deconnecter du serveur", pr);
						sendMessage("2-Port <numero De port> : pour modifier le numero de port serveur", pr);
						sendMessage("3-ajouter: ajouter un nouveau produit ", pr);
						sendMessage("4-getall: voir tous les produits ", pr);
						sendMessage("-----------------------------------------", pr);

						break;
					case ("down"):
						sendMessage("Fermeture Du Client", pr);
						id = false;
						s.close();
						flux_entree.close();
						System.out.println("\ndeconnexion du client");
						break;
					case ("ajouter"):
						sendMessage("--- Début de la procédure d'ajout d'un produit ---", pr);
						System.out.println("--- Début de la procédure d'ajout d'un produit ---");
						
						boolean vef = ajouterproduit();
						if (vef) {
							sendMessage("votre produit a ete bien ajouter", pr);
							System.out.println("\nbien ajoute");
						} else {
							sendMessage("un probleme est servenu lors de l'ajout de votre produit", pr);
							System.out.println("\n un probleme est servenu lors de l'ajout de votre produit");
						}
						break;
					case ("getall"):
						sendMessage("-------- LISTE DE TOUS LES PRODUITS --------", pr);
						System.out.println("-------- LISTE DE TOUS LES PRODUITS --------");
						
						allproducts();
						
						break;
					case("port"):
						if(entreSplit.length == 2) {
							int porte=50004;
							try {
							porte=Integer.parseInt(entreSplit[1]);
							
							sendMessage("Fermeture Du Client", pr);
							flux_entree.close();
							pr.close();
							s.close();
							ss.close();
							id=false;
							ss = initSocktetS(porte);
							System.out.println(porte);
							sendMessage("", pr);
							}catch (NumberFormatException e) {
								System.out.println("veuiller entre le bon paramétre ");
								sendMessage("Format non respecter", pr);
							}
							
						}
						else
							sendMessage("une erreur dans la commande entrée", pr);
							
						break;
					default:
						
					try {InetAddress i=s.getInetAddress();	
						Log.write(getTime()+" Ip d'envoie :"+ i + "\n");
						Log.write(getTime()+" message reçue et rejeté : "+flux_entree_lu+ "\n") ;}
					catch (Exception e) {
				System.out.println(	"impossible d\'ouvrire le fichier");
						}
						sendMessage("Ce parametre n\'est pas pris en compte", pr);
						System.out.println("paramettre rejeté  "+flux_entree_lu);
						break;
					}
				
				}

			}

		}

	}
	//fonction qui recupére des fichier depuis la base de donnée et les envoie au client 
	public static void allproducts() throws SQLException,IOException{
			
			
			PreparedStatement st = null;
			ResultSet rs = null;
		
			
			pr = new PrintWriter(s.getOutputStream());		
			st = bd.prepareStatement("SELECT * FROM categorie");
			rs = st.executeQuery();
			
			while(rs.next()) {
				System.out.print("AllProducts categorie : ");
			    System.out.println(rs.getString(1));
			    sendMessage("CATEGORIE: "+rs.getString(1), pr);
			    PreparedStatement query2 = null;
				ResultSet result2 = null;
				System.out.println("getting the products of the categorie: ");
				pr = new PrintWriter(s.getOutputStream());		
				query2 = bd.prepareStatement("SELECT * FROM produit where produit.categorieid=? ");
				query2.setInt(1,rs.getInt(2));
				result2 = query2.executeQuery();
				while(result2.next()) {
					
					sendMessage("nom : "+result2.getString(2), pr);
					sendMessage("prix : "+result2.getString(3), pr);
					sendMessage("quantite en stock : "+result2.getString(4), pr);
					sendMessage("etat : "+result2.getString(8), pr);
					sendMessage("description : "+result2.getString(12), pr);
				
					sendMessage("----------------------------------------",pr);
				}
				System.out.println("Products download");
			
			}
			System.out.print("tout les produits sont charger ");
			sendMessage("------------------ EnD --------------------", pr);
			
		}
	
	// via un dialogue avec le client le serveur  construit la requet pour ajouter un produit dans la base de donnée
	private static boolean ajouterproduit() throws IOException, SQLException {
			PreparedStatement st = null;
			ResultSet rs = null;
			Boolean verif = false;
			int qte=-1;
			int prix=-1;
			pr = new PrintWriter(s.getOutputStream());
			sendMessage("choisir la categorie du produit que vous voulez ajouter", pr);
			st = bd.prepareStatement("SELECT * FROM categorie");
			rs = st.executeQuery();
			 System.out.println("récupération des nom des catégories  depuis la base de donnée");
			while(rs.next()) {
				
			    sendMessage(rs.getInt(2)+" :"+rs.getString(1), pr);
			} System.out.println("récupération ... OK");
			sendMessage("-----------------------------------", pr);
			
			flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
			flux_entree_lu = flux_entree.readLine();
			if(flux_entree_lu == null){
				return false;
			}
			
			
			int idcate=-1;
			try {
			idcate=Integer.parseInt(flux_entree_lu);
			}catch (NumberFormatException e) {
				
				System.out.println("l'entrée n'est pas valide");
				return false;
			}
			int idmarque;
			int idfour;
		
			if (idcate == -1) {
				
				return false;
				
			}else
			{	sendMessage("--- Choix du fournisseur ---", pr);
				sendMessage("choisir le fournisseur du produit que vous voulez ajouter", pr);
				st = bd.prepareStatement("SELECT * FROM fournisseur");
				rs = st.executeQuery();
				
				while(rs.next()) {
				//	System.out.print("Column 1 returned ");
				//  System.out.println(rs.getString(2));
				    sendMessage(rs.getInt(10)+" :"+rs.getString(1), pr);
				}sendMessage("-----------------------------------", pr);
				
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				try {
				idfour=Integer.parseInt(flux_entree_lu);
				}catch (NumberFormatException e) {
					System.out.println("l'entrée n'est pas valide");
					return false;
				}
				sendMessage("--- Choix de la marque ---", pr);
				sendMessage("choisir la marque du produit que vous voulez ajouter", pr);
				st = bd.prepareStatement("SELECT * FROM marque");
				rs = st.executeQuery();
				
				while(rs.next()) {
					//System.out.print("Column 1 returned ");
				    //System.out.println(rs.getString(2));
				    sendMessage(rs.getInt(2)+" :"+rs.getString(1), pr);
				}sendMessage("-----------------------------------", pr);
				
				
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				try {
				idmarque=Integer.parseInt(flux_entree_lu);
				}catch (NumberFormatException e) {
					System.out.println("l'entrée n'est pas valide");
					return false;
				}
				sendMessage("veuillez entrez le nom du produit", pr);
				
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				String nomP=flux_entree_lu;
				sendMessage("veuillez entrez le prix du produit", pr);
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				try {
				 prix=Integer.parseInt(flux_entree_lu);
				}catch (NumberFormatException e) {
					System.out.println("l'entrée n'est pas valide");
					return false;
				}
				 sendMessage("veuillez entrez la quantite du produit", pr);
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}	
				try {
				qte=Integer.parseInt(flux_entree_lu);
				}catch (NumberFormatException e) {
					System.out.println("l'entrée n'est pas valide");
					return false;
				}
				sendMessage("veuillez entrez l'etat du produit", pr);
				
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				String etat=flux_entree_lu;
				sendMessage("veuillez entrez la description du produit", pr);
				flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));
				flux_entree_lu = flux_entree.readLine();
				if(flux_entree_lu == null){
					return false;
				}
				String desc=flux_entree_lu;
				
				try {
				st = bd.prepareStatement("INSERT INTO produit (produitid,nomproduit,prixunitaire,quantiteenstock,etat,categorieid\n" + 
						"					,marqueid,idfournisseur,descproduit)\n" + 
						"			VALUES (nextval('produit_produitid_seq'::regclass),?,?,?,?,?,?,?,?)");
				
				st.setString(1, nomP);
				st.setInt(2,prix);
				st.setInt(3,qte);
				st.setString(4, etat);
				st.setInt(5,idcate);
				st.setInt(6,idmarque); 	
				st.setInt(7,idfour);
				st.setString(8,desc);
				
				int vv = st.executeUpdate();
				if(vv>0) {
					
					verif=true;
				}
				}    
            catch (PSQLException e) {
                System.out.println("une contrainte de la bd empeche de realiser l'insertion");
                verif=false;
            }
			}
			
			return verif;
		}
	
	//verfie l'id mot de passe dans la base de donnée puis retourne une confirmation au client 
	//si la connection est confirmer alors il a le droit de continuer le dialogue avec le serveur sinon il 
	//dois ressayer ! maximum 3 essie par connexion
	public static Boolean verificationID() throws IOException, SQLException {
		PreparedStatement st = null;
		ResultSet rs = null;
		Boolean verif = false;
		try
		{
		
		 Log = new FileWriter(filename,true);
		 
		 
		}
		catch(IOException e)
		{
		System.out.println("impossible d'ouvrire le fichier ");
		
		}
		if(ss !=null) {
		try {
			
		s = ss.accept();
		s.setSoTimeout(10000);
		pr = new PrintWriter(s.getOutputStream());
		
		//sendMessage("veuillez entrer un nom d\'utilisateur et un mot de passe", pr);
		System.out.println("verification des identifiants");
		flux_entree = new BufferedReader(new InputStreamReader(s.getInputStream()));

		flux_entree_lu = flux_entree.readLine();
		if (flux_entree_lu != null) {
			
			str = flux_entree_lu.split(" ");
			if(str[0].equals("down")) {
				sendMessage("Fermeture Du Client", pr);
				id = false;
				
				pr.close();
				flux_entree.close();
				s.close();
				System.out.println("\ndeconnexion du client");
			}else	if (str.length == 2) {
				st = bd.prepareStatement("SELECT * FROM admin WHERE email= ? and password=?");
				String email = str[0];
				String pwd = str[1];
				st.setString(1, email);
				st.setString(2, pwd);

				rs = st.executeQuery();

				if (rs.next()) {
					verif = true;
					essaie = 0;
					sendMessage("Connection done", pr);
					System.out.print("Connection établie\n");

				} else {
					verif = false;
					essaie++;
					InetAddress i=s.getInetAddress();
					Log.write(getTime()+" Ip d'envoie :"+ i+"\n");
					Log.write(getTime()+" message reçue et rejeté : "+flux_entree_lu+"\n") ;
					System.out.println("Nom d\'utlisateur ou mot de passe errone essaie restant :"+(3 - essaie)  );
					sendMessage("Nom d\'utlisateur ou mot de passe errone essaie restant :" + (3 - essaie)  , pr);
				

				}
				rs.close();
				st.close();
			} else {

				verif = false;
				essaie++;
				if (essaie == 3) {
					InetAddress i=s.getInetAddress();
					Log.write(getTime()+" Ip d'envoie :"+ i+"\n");
					Log.write(getTime()+" message reçue et rejeté : "+flux_entree_lu+"\n") ;
					System.out.println("Echec de la connexion ");
					sendMessage("Fermeture Du Client", pr);
					id = false;
					pr.close();
					flux_entree.close();
					s.close();
					
				} else
					
					System.out.println("Nom d\'utlisateur ou mot de passe errone essaie restant :" + (3 - essaie) + "\n");
					sendMessage("Nom d\'utlisateur ou mot de passe errone essaie restant :" + (3 - essaie) + "\n", pr);
				
			}

		} 

		

		}catch (SocketTimeoutException e) {
			System.out.println("Le client a pris trop de temps à repondre le serveur s'est couper");
			
		}catch (IOException e) {
			
			
		}
		}
		return verif;
	}
	
	//permet d'envoyer des message au client tout en ecrivent ceux-ci dans le fichier des log
	public static void sendMessage(String message, PrintWriter re) throws IOException {

		re.println(message);
		//Log.write(getTime() +" reponse : "+message+"\n" );
		re.flush();

	}
	
	// etablier une connexion a la base de donnée 
	public static Connection initConnexionBD() throws IOException {
		try {
			// register Postgresql jbc driver
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {

		System.out.println("une erreur est survenue");
		
		}

		Connection conn = null;
		System.out.println("Driver Registered Successfully ....");

		// Etablire la connexion a la base de donn�e avec verfication des id ainsi de
		// la bonne conduite de la connection
		// Avec celle-ci
		try {
			// creating the connection
			conn = DriverManager.getConnection(
					"jdbc:postgresql://postgresql-ecommerce1.alwaysdata.net:5432/ecommerce1_db", "ecommerce1",
					"Jb_58227277685");
			System.out.println("the connection with the database has been established ");
		} catch (SQLException e) {
			System.out.println("Unable to creat the connection ");
			
			
		}
		
		
		return conn;
	}
	
	//initialise la Socket serveur et ce met a l'ecoute 
	static ServerSocket initSocktetS(int port) throws UnknownHostException  {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			
			System.out.println("Server whaiting ....");
		} catch (IOException e) {
			System.out.println("le port est deja utiliser");
			
			sortie=false;
			
		}	
		
	
		return ss;
		
	}

	//affiche  une err et ferme la liaison avec le client 
	private static void errClient() throws IOException {
		System.out.println("un probleme c'est produit \n");
		Log.write(getTime()+" serveur : un probleme c'est produit \n");
		Log.close();
		id = false;
		pr.close();
		
		
		flux_entree.close();
		s.close();

	}
	
	//sert a recupéré l'heur /et le jour mois année 
	private static String getTime() {
		LocalDateTime myDateObj = LocalDateTime.now();

		DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

		    String formattedDate = myDateObj.format(myFormatObj);
		return formattedDate;
	}
	
	
	
	
}