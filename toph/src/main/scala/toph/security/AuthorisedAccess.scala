package toph.security

import toph.model.Account

case class AuthorisedAccess(
    account: Account,
    accessToken: Array[Byte],
    refreshToken: String,
)
