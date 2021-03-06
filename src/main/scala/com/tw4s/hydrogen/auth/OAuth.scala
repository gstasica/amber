package com.tw4s.hydrogen.auth

import javax.crypto
import spray.http.HttpRequest
import spray.http.HttpRequest
import java.net.URLEncoder
import scala.collection.immutable.TreeMap
import spray.http.HttpEntity
import spray.http.ContentType
import spray.http.MediaTypes
import java.nio.charset.Charset
import org.parboiled.common.Base64
import spray.http.HttpHeaders.RawHeader

object OAuth {

  case class Consumer(key: String, secret: String)
  case class Token(value: String, secret: String)

  def oAuthAuthorizer(consumer: Consumer, token: Token): HttpRequest => HttpRequest = {

    val timestamp = (System.currentTimeMillis / 1000).toString
    // unique token for each unique request
    val nonce = System.nanoTime.toString

    //construct the key & crypto entity
    val SHA1 = "HMAC-SHA1"
    val keyString = percentEncode(consumer.secret :: token.secret :: Nil)
    val key = new crypto.spec.SecretKeySpec(bytes(keyString), SHA1)
    val mac = crypto.Mac.getInstance(SHA1)

    { httpRequest: HttpRequest =>

      // pick out x-www-form-urlencoded body
      val (requestParams, newEntity) = httpRequest.entity match {
        case HttpEntity.NonEmpty(ContentType(MediaTypes.`application/x-www-form-urlencoded`, _), data) =>
          val params = data.asString.split("&")
          val pairs = params map { param =>
            val p = param.split("=")
            p(0) -> percentEncode(p(1))
          }

          (pairs.toMap, HttpEntity(ContentType(MediaTypes.`application/x-www-form-urlencoded`), "%s=%s" format (pairs(0)._1, pairs(1)._2)))
        case e => (Map(), e)
      }

      val oAuthParams = Map(
        "oauth_consumer_key" -> consumer.key,
        "oauth_nonce" -> nonce,
        //"oauth_signature"->"signs all header & request params, as such can't be calc here",
        "oauth_signature_method" -> "HMAC-SHA1",
        "oauth_timestamp" -> timestamp,
        "oauth_token" -> token.value,
        "oauth_version" -> "1.0")

      val encodedOrderedParams = (TreeMap[String, String]() ++ oAuthParams) map { case (k, v) => k + "=" + v } mkString "&"
      val url = httpRequest.uri.toString
      // construct the signature base string
      val signatureBaseString = percentEncode(httpRequest.method.toString() :: url :: encodedOrderedParams :: Nil)

      mac.init(key)
      val sig = Base64.rfc2045().encodeToString(mac.doFinal(bytes(signatureBaseString)), false)
      mac.reset()

      val oauth = TreeMap[String, String]() ++ (oAuthParams + ("oauth_signature" -> percentEncode(sig))) map { case (k, v) => "%s=\"%s\"" format (k, v) } mkString ", "

      //return signed httpRequest
      httpRequest.withHeaders(List(RawHeader("Authorization", "OAuth " + oauth))).withEntity(newEntity)
    }
  }

  private def percentEncode(str: String): String = URLEncoder.encode(str, "UTF-8") replace ("+", "%20") replace ("%7E", "~")
  private def percentEncode(s: Seq[String]): String = s map percentEncode mkString "&"
  private def bytes(str: String) = str.getBytes(Charset.forName("UTF-8"))

}