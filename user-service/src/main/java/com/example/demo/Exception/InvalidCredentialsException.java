package com.example.demo.Exception;

public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException(String message) {
        super(message);
    }
}