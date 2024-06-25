package com.api.gestion.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FacturaUtils
{
	public FacturaUtils() {}
	
	public static ResponseEntity<String> getResponseEntity(String messsage, HttpStatus httpStatus)
	{
		return new ResponseEntity<String>("Mensaje : " + messsage, httpStatus);
	}
}
