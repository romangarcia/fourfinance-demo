# 4Finance Homework

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