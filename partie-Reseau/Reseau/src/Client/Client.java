package Client;


import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStreamReader ;
import java.io.PrintWriter ;
import java.net.Socket ;
import java.net.SocketTimeoutException;



public class	Client {
	 private static Socket socket = null ;
     private static PrintWriter flux_sortie = null ;
     private static BufferedReader flux_entree = null ;
	     private static String chaineSortie="" ;
	     private static String chaineEntre="";
     static String socketIntit ="";
     private static String Sortie="";
     private static Boolean id=false;
     private static int port=50005;
     private static  BufferedReader entree_standard;
     
       
		public static void main (String argv []) throws IOException {
	       


	        // L'entree standard
	       entree_standard= new BufferedReader ( new InputStreamReader ( System.in)) ;
	       configuration();
	        
	        do {
	       
	        	if(id != true && Sortie!=null) {

	        		connexion();
	        		
	        	}
	        	
	        	
	        
	        		
	        	if(id==true){
	        		System.out.println("");
	        		System.out.println("Entrée votre Commande // help si vous avez besoin d'aide ");
	        		chaineSortie=entree_standard.readLine ();
	        		
	        		
	        		System.out.println("");
	        		try {
	        		
	        			
		        	flux_sortie.println (chaineSortie) ;
		        
		        	
		        	
		        	
		        	
		        	 
		        	chaineEntre=flux_entree.readLine();
		        		  
		        		

		        
		        	
		        		
		        	if(chaineEntre!=null){
		        		
		        	if(chaineEntre.equals("-----? HELP ?-----")){
		        		System.out.println (" " ) ;
		        		System.out.println ("serveur: ") ;
		        		System.out.println(chaineEntre);
                        chaineEntre=flux_entree.readLine();
		        		while(!chaineEntre.equals("-----------------------------------------")){
		        			
                            System.out.println ("" + chaineEntre) ;
                            chaineEntre=flux_entree.readLine();
                        }
		        		System.out.println(chaineEntre);
		        		
		        	}else if((chaineEntre.equals("--- Début de la procédure d'ajout d'un produit ---")
		        			|| chaineEntre.equals("--- Choix du fournisseur ---")
		        			|| chaineEntre.equals("--- Choix de la marque ---") )){
		        		System.out.println (" " ) ;
		        		System.out.println ("serveur: ") ;
		        		
		        		System.out.println(chaineEntre);
		        		chaineEntre=flux_entree.readLine();		
		        		while(!chaineEntre.equals("-----------------------------------")){
		        			
                            System.out.println ("" + chaineEntre) ;
                            chaineEntre=flux_entree.readLine();
                        }	
		        		System.out.println(chaineEntre);
		        	}else if(chaineEntre.equals("-------- LISTE DE TOUS LES PRODUITS --------")){
		        		System.out.println (" " ) ;
		        		System.out.println ("serveur: ") ;
		        		System.out.println(chaineEntre);
                        chaineEntre=flux_entree.readLine();
		        		while(!chaineEntre.equals("------------------ EnD --------------------")){
		        			
                            System.out.println ("" + chaineEntre) ;
                            chaineEntre=flux_entree.readLine();
                        }
		        		System.out.println(chaineEntre);
		        	}else if(chaineEntre.equals("Fermeture Du Client")) {
			        	System.out.println(chaineEntre);
			        	Sortie=null;	
			        	}else  {
			        		System.out.println(chaineEntre);
			        	}
			       
	        		}
	        	else if(socket.isConnected() && chaineEntre==null) {
	        		 System.out.println("le serveur a subi une coupure inattendue" );
	        		Sortie=null;
	        	}}catch (SocketTimeoutException e) {
		        	Sortie=null;
		        	socketIntit=null;
		        	System.out.println("Le serveur a pris trop de temps à repondre ");
					
				}
	        		}
	        	
	  
	        	        } while (Sortie != null) ;
	      
	        if(socketIntit !=null) {
	        	flux_sortie.close () ;
	  	        flux_entree.close () ;
	  	        entree_standard.close () ;
	  	        socket.close () ;
	        }
	      
	    }
		
		
		
		
		
		
		
		
		public static Boolean verifID(String read) throws IOException {
			boolean verif=false;
			if(read	!=null) {
			if(read.equals("Connection done")){
				System.out.println("Connection done");
				verif=true;
				
			}else {
				if(read.equals("fermeture")) {
					System.out.println("Server : "+read);
					chaineEntre=null;
				}else {
					System.out.println("Server : "+read);
					
				}
				
				flux_sortie.close () ;
		        flux_entree.close () ;
		        
				socket.close();
				
				
				
			}
			}
			
			return verif;
			
		}
		public static void connexion() throws IOException {
		    try {
		            
		            socket = new Socket ("127.0.0.1", port) ;
		            socket.setSoTimeout(10000);
		            flux_sortie = new PrintWriter (socket.getOutputStream (), true) ;
		            flux_entree = new BufferedReader (new InputStreamReader (
		                                        socket.getInputStream ())) ;
		            
		            if(flux_entree!=null) {
		      		  System.out.println ("veuillez entrer un nom d\'utilisateur et un mot de passe") ;
		      		
		      		 
		      		  chaineSortie=entree_standard.readLine ();
		      		  flux_sortie.println (chaineSortie) ;
		      		  chaineEntre=flux_entree.readLine();
		      		  
		      		  if(chaineEntre !=null && !chaineSortie.equals("down") && !chaineEntre.equals("Fermeture Du Client")) {	
		      			  
		        	        	id=verifID(chaineEntre);
		      		  }else  {
		      			  if(chaineEntre == null) {
		      				  System.out.println("le serveur a subi une coupure inattendue");
		      			  }
		      			  
		      			  
		      			  
		      			  id=false;
		      			  Sortie=null; 
		      		  }
		    	        	  	
		      		 
		      		} 
		        } catch (SocketTimeoutException e) {
		        	Sortie=null;
		        	socketIntit=null;
		        	System.out.println("Le serveur a pris trop de temps à repondre ");
					
				}
		        catch (IOException e) {
		        	Sortie=null;
		        	socketIntit=null;
		        	System.out.println("erreur le port spécifié n'est pas ouvert");
	        		
		        } 
    		
		}
		public static void configuration() throws IOException {
			if(id==false) {
				
			System.out.println("\navant de lancer la connexion il vous faut configuree quelque  parametre ");
			System.out.println("le port sur le quel vous ete actuellemnt connecte sur le port : " +port);
			System.out.println("pour changer le port du serveur vous devez vous connecter dans un premier temp");
			System.out.println("puis tapez help");
			System.out.println("si vous voulez changer le port taper 1");
			System.out.println("si vous voulez vous deconnecte taper 2");
			System.out.println("sinon appuier sur entree");
			}else {
				System.out.println("le port sur le quel vous ete actuellemnt connecte sur le port : " +port);
				System.out.println("si vous voulez changer le port taper 1");
				System.out.println("sinon appuier sur entree");
			}
			String entree =entree_standard.readLine ();
			
			switch (entree) {
			
			case "1":
				if(id=false) {
				try{System.out.println("entre le numero de port  pour <client> ");
				System.out.println("entree le numero de port ");
				String entreTmp =entree_standard.readLine ();
				
					port=Integer.parseInt(entreTmp);
					
			}catch (NumberFormatException e) {
				System.out.println("veiller entre un chiffre ");
				
			}
				}else {
					try{System.out.println("entre le numero de port  pour <client> ");
					System.out.println("entree le numero de port ");
					String entreTmp =entree_standard.readLine ();
					
						port=Integer.parseInt(entreTmp);
						
				}catch (NumberFormatException e) {
					System.out.println("veuiller entre un chiffre ");
					
				}
					
				}
				
			
				break;
			
			case "":
				
				
				break;
			
			case "2":
				socketIntit=null;
				Sortie=null;
				break;
			default:
				System.out.println();
				break;
			}
			
		}
}
