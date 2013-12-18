package com.tw4s.hydrogen.auth

import spray.http.HttpRequest

object OAuth {
  
  case class Consumer(key: String, secret: String)
  case class Token(value: String, secret: String)
  
  def authorizer(consumer: Consumer, token : Token): HttpRequest => HttpRequest = {
    ???
  }
  
}