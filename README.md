-- 4Finance Homework --

-- Install [http://www.scala-sbt.org][SBT] --

. Linux:
  > sudo apt-get install sbt
. Windows
  > Download from [http://www.scala-sbt.org][SBT Download Page] and run install.

-- Run Rest Server --
  > sbt start     ## will download dependencies (takes some time) and start server

-- Test Loan Request --
  > curl -X POST 'http://localhost:9000/loans' -HContent-Type:application/json -d'{ "first_name" : "Roman", "last_name" : "Garcia", "amount" : 4999.99, "term" : "5 days" }'

--