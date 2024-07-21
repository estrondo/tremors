package toph.security

import toph.model.Account

case class Token(
    account: Account,
    token: String,
)
