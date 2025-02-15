package toph.security

import toph.model.Account

case class AccessToken(
    account: Account,
    token: Array[Byte],
)
