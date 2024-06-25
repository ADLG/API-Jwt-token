package com.api.gestion.service.imp;

import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.api.gestion.constantes.FacturaConstantes;
import com.api.gestion.dao.RepoUserDAO;
import com.api.gestion.pojo.User;
import com.api.gestion.security.CustomerDetailsService;
import com.api.gestion.security.jwt.JwtUtil;
import com.api.gestion.service.UserService;
import com.api.gestion.util.FacturaUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImp implements UserService
{
	@Autowired
	private RepoUserDAO repoUser;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomerDetailsService customerDetailsService;

	@Autowired
	private JwtUtil jwtUtil;
	
	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap)
	{
		log.info("Registro interno de un usuario)", requestMap);
		try
		{
			if (validateSignUpMap(requestMap))
			{
				User user = repoUser.findByEmail(requestMap.get("email"));
				if (Objects.isNull(user))
				{
					repoUser.save(getUserFromMap(requestMap));
					return FacturaUtils.getResponseEntity("Usuario registrado con éxito", HttpStatus.CREATED);
				}
				else
				{
					return FacturaUtils.getResponseEntity("El usuario con ese email ya existe", HttpStatus.BAD_REQUEST);
				}
			}
			else
			{
				return FacturaUtils.getResponseEntity(FacturaConstantes.INVALID_DATA, HttpStatus.BAD_REQUEST);
			}
		}catch (Exception exeption)
		{
			exeption.printStackTrace();
		}
		return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap)
	{
		log.info("Dentro de login");
		try
		{
			Authentication authentication = authenticationManager.authenticate
			(
				new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
			);

			if (authentication.isAuthenticated())
			{
				if (customerDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true"))
				{
					return new ResponseEntity<String>(
						"{\"token\":\"" + jwtUtil.generateToken(customerDetailsService.getUserDetail().getEmail(),
							customerDetailsService.getUserDetail().getRole()) +"\"}", HttpStatus.OK);
				}
				else
				{
					return new ResponseEntity<String>("{\"mensaje\":\"" + " Espera la aprobación del administrador "+"\"}", HttpStatus.BAD_REQUEST);
				}
			}
		}catch(Exception e)
		{
			log.error("{}",e);
		}
		return new ResponseEntity<String>("{\"mensaje\":\"" + " Credenciales incorrectas "+"\"}", HttpStatus.BAD_REQUEST);
	}

	private User getUserFromMap(Map<String,String> requestMap)
	{
		User user = new User();
		user.setNombre(requestMap.get("nombre"));
		user.setNumeroDeContacto(requestMap.get("numeroDeContacto"));
		user.setEmail(requestMap.get("email"));
		user.setPassword(requestMap.get("password"));
		user.setStatus("false");
		user.setRole("user");
		return user;
	}

	private Boolean validateSignUpMap(Map<String,String> requestMap)
	{
		if (requestMap.containsKey("nombre") && requestMap.containsKey("numeroDeContacto") && requestMap.containsKey("email") && requestMap.containsKey("password"))
		{
			return true;
		}
		return false;
	}

}
