package openid.zio

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom

def keyPairGenerator(): KeyPair =
  val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
  keyPairGenerator.initialize(1024, SecureRandom.getInstance("SHA1PRNG", "SUN"))
  keyPairGenerator.genKeyPair()
