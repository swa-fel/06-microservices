/**
 * Copyright 2011-2017 GatlingCorp (http://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package swafel

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class RemoteSwafelShopWithCheckSimulation extends Simulation {

  val httpConf = http
    .baseURL("http://shop.swafel.com") // Here is the root for all relative URLs

  val scn = scenario("Shop Service") // A scenario is a chain of requests and pauses
    .exec(http("request_1")
      .get("/item/1")
      .check(substring("in stock!")))
    .pause(1) // Note that Gatling has recorded real time pauses

  setUp(scn.inject(rampUsersPerSec(25) to (25) during(100)).protocols(httpConf))
}
