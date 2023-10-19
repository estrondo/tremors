package toph.model

import toph.security.Claims

case class AuthenticatedUser(token: String, expiration: Long, claims: Claims)
