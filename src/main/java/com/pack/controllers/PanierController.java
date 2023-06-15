package com.pack.controllers;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pack.ConvertDate;
import com.pack.models.Commande;
import com.pack.models.Panier;
import com.pack.models.StatAnnulleToken;


import com.pack.models.Token;
import com.pack.models.User;
import com.pack.payload.response.PaiementRetour;
import com.pack.service.CommandeService;
import com.pack.service.PanierService;
import com.pack.service.SoldeService;
import com.pack.service.StatistiqueAnnuelTokenService;

import com.pack.service.TokenService;
import com.pack.service.UserService;

@CrossOrigin(origins = "*")
@RequestMapping("/api/test")
@RestController
public class PanierController {

	@Autowired
	PanierService panierService;
	@Autowired
	CommandeService commandeService;
	@Autowired
	SoldeService soldeService;
	@Autowired
	ConvertDate convertDate;
	@Autowired
	TokenService tokenService;
	@Autowired
	StatistiqueAnnuelTokenService statistiqueAnnuelServiceToken;
	

	@Autowired
	UserService userService;
	@PreAuthorize("hasRole('ROLE_CLIENT')")

/*Gérer panier  */
// consulter panier by user 
@RequestMapping(method = RequestMethod.GET, value = "/paniers/{username}")
public List<Panier> getPanierByUser(@PathVariable String username) {
	  return panierService.getPaniersByUser(username);

}

// Payer Panier

	@RequestMapping(method = RequestMethod.DELETE, value = "/paniers/{id}")
	public PaiementRetour payerPanier(@PathVariable Long id , Authentication authentication) {
		String username = authentication.getName();
		User user =userService.getUserByUsername(username);
       // meesage de transaction 
		PaiementRetour retour = new PaiementRetour();
        // new commande 
		Commande commande = new Commande();
		
		StatAnnulleToken statistiquetoken=new StatAnnulleToken();

		double montantPanier = 0;
		long id_token;
		Date date = new Date();
		int annee,mois,nb_mois,nb_annee;
		long idstatAnne;
		Boolean anneeExist=false,moisExist=false;


		// new panier 
		Panier panier = new Panier();
		// new token 
		Token token=new Token();

		
		panier = panierService.getPanierById(id);
		System.out.println("panier " + panier.getId());
		token=panier.getToken();
		System.out.println("token " + token.getId());
		String type_token= panier.getToken().getTypetoken().getNom();
	     //extraction annee et mois
		ConvertDate c = new ConvertDate();
		annee=c.retournerAnnee(date);
		mois=c.retournerMois(date);

// verifier solde a pour ce panier 
		if (soldeService.verifierSolde(panier)) {
	
            // ajout date au  commande
			commande.setDate(convertDate.cenvertirDate(date));
			//commande prendre panier 
			commande.setPanier(panier);
			// commande de user 
			commande.setUser(user);
			// ajouter commande 
			commandeService.addCommande(commande);
			// changer etat de panier 
			panier.setActive(false);
			// changer etat de token 
			token.setActive(false);
			panierService.addPanier(panier);


	       // Debiter solde 
			soldeService.soustraire(panier);
			
	       //suppression du token 
			tokenService.updateToken( token);
			// ajouter stat 
			statistiqueAnnuelServiceToken.ajouterStatAnnuel(date, type_token);
			retour.setSuccess(true);
			retour.setMessage("Paiement effectué avec succès");

		} 
		else {
			retour.setSuccess(false);
			retour.setMessage("Echec de Paiement , votre solde est insuffisant");
		}
		
		return retour ; 
	}



	@RequestMapping(method = RequestMethod.GET, value = "/paniers")
	public List<Panier> getPanier()

	{
		panierService.getAllPanier().forEach(t -> {
			System.out.println(t.toString());
		});
		return (List<Panier>) panierService.getAllPanier();

	}


	@RequestMapping(method = RequestMethod.POST, value = "/paniers")
	public void addPanier(@RequestBody Panier panier) {
		System.out.println(panier.toString());
		panierService.addPanier(panier);
	}

	@RequestMapping("/paniers/{id}")
	public Optional<Panier> getSinglePanier(@PathVariable Long id) {
		return panierService.getSinglePanier(id);
	}


	@RequestMapping(method = RequestMethod.PUT, value = "/paniers/{id}")
	public void updatePanier(@RequestBody Panier panier, @PathVariable Long id) {
		System.out.println(panier.toString());
		panierService.updatePanier(id, panier);
	}

	

}
