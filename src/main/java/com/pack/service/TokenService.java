package com.pack.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.pack.models.Token;

import com.pack.models.Typetoken;
import com.pack.models.User;
import com.pack.repository.TokenRepository;
import com.pack.repository.TypetokenRepository;


@Component
public class TokenService {

	@Autowired
	private TokenRepository tokenRepo;
	
	@Autowired
	private UserService userService;
	
/*Gérer token */

//Consulter token par user dans liste   
	 public List<Token> getTokensByUser(Authentication authentication) {
		String username = authentication.getName();
		User client = userService.getUserByUsername(username);
		List<Token> listtokens=new ArrayList();
		tokenRepo.findAllByUserId(client.getId()).forEach(t->{
			if(t.getActive())
				listtokens.add(t);
		});
		return listtokens;
	}

		
// Ajouter Token 
	    public ResponseEntity<Token> saveToken(Token token, Authentication authentication) {
		String username = authentication.getName();
			User client = userService.getUserByUsername(username);
			token.setUser(client);
	    //modifier numero token génerer
			token.setNumerotoken(this.genererNumeroToken(token.getTypetoken().getId(),token.getCompteur().getId(), client.getId()));
			Token TokenSaved = tokenRepo.save(token);
			return ResponseEntity.ok(TokenSaved);
		}

	

		
   /* generate token user  */
	public String genererNumeroToken( Long idTypeToken, Long idCompteur, Long idUser) {
		// heure actuelle miliseconde + idTypeToken + idCompteur + idUser 
		String numeroToken=""+System.currentTimeMillis()+idTypeToken+idCompteur + idUser;
		return numeroToken;
	}
	

// Modifier Token 
	public void updateToken(Token token) {
		Token tokenBd = tokenRepo.findById(token.getId()).get();
		tokenBd.setNumerotoken(token.getNumerotoken());
		tokenBd.setCompteur(token.getCompteur());
		tokenBd.setTypetoken(token.getTypetoken());
		tokenRepo.save(tokenBd);	
			
	}

	// suuprimer token 
	
		public void deleteToken(Long id) {
			tokenRepo.deleteById(id);
		}






		
		
	public List<Token> getAllToken() {
		return tokenRepo.findAll();
	}
	
	
	public Optional<Token> getSingleToken(Long id) {
		return tokenRepo.findById(id);
	}
		 



	/*public List<Token> getTokensByUser(Authentication authentication) {
		String username = authentication.getName();
		User client = userService.getUserByUsername(username);
		List<Token> listtokens=new ArrayList();
		tokenRepo.findAllByUserId(client.getId()).forEach(t->{
			if(t.getActive())
				listtokens.add(t);
		});
		return listtokens;
	}*/
	
}
