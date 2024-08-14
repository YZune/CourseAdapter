package exception

class NetworkErrorException(message: String) : Exception(message)

class ServerErrorException(message: String) : Exception(message)

class UserNameErrorException(message: String) : Exception(message)

class PasswordErrorException(message: String) : Exception(message)

class CheckCodeErrorException(message: String) : Exception(message)

class QueuingUpException(message: String) : Exception(message)

class GetTermDataErrorException(message: String) : Exception(message)

class EmptyException(message: String) : Exception(message)