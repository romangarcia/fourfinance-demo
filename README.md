# 4Finance Homework

This project implements the Technical Assesment for 4Finance.

It was designed and implemented for the JVM Platform using the Scala language.

I took the liberty to avoid both requested libraries, Spring and Hibernate, with the intention to keep a
simple implementation for a really simple requirement.

I used Play Framework 2.4 as MVC (REST only in this case), MacWire for Compilation-Time DI, Anorm as SQL / JDBC abstraction
and ScalaTest for unit testing.

### Install [SBT](http://www.scala-sbt.org)

Download from [SBT Download Page](http://www.scala-sbt.org/download.html) and install.

### Run Server

  > sbt start  ## downloads dependencies and starts server

### Test Loan Request
```
curl -X POST 'http://localhost:9000/loans' -HContent-Type:application/json -d'{ "first_name" : "Roman", "last_name" : "Garcia", "amount" : 4999.99, "term" : "5 days" }'
```

NOTE: From the fourth request on from the same client you should see a "rejection" error

NOTE (2): The "term" property accepts "natural" duration syntax. i.e.: "5 seconds", "2 months", "15 years", etc.

## Run Tests
  > sbt test
