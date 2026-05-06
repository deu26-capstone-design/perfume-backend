package kim.biryeong.perfume.auth;

class AuthConflictException extends RuntimeException {

  AuthConflictException(String message) {
    super(message);
  }
}

class AuthUnauthorizedException extends RuntimeException {

  AuthUnauthorizedException(String message) {
    super(message);
  }
}

class InvalidCredentialsException extends RuntimeException {

  InvalidCredentialsException() {
    super("invalid credentials");
  }
}
