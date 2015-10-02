Galeb Manager
===========================
[![Circle CI](https://circleci.com/gh/galeb/galeb-manager.svg?style=svg)](https://circleci.com/gh/galeb/galeb-manager)

Galeb Manager is a Galeb Router manager built on Spring Boot.<br/>
Galeb Router is a massively parallel routing system running a shared-nothing architecture.

Its main features are:
* Open Source
* API REST (management)
* Simultaneously managing multiple and different farm types (Galeb Router Farm only, for now)
* Allows dynamically change routes and configuration without having to restart or reload
* Highly scalable

Building
-----

<code>
$ mvn clean test #optional
</code>

<code>
$ mvn clean package -DskipTests
</code><br/>

Using
-----

<code>
$ mvn spring-boot:run
</code><br/>

# License

```Copyright
Copyright (c) 2014-2015 Globo.com - All rights reserved.

 This source is subject to the Apache License, Version 2.0.
 Please see the LICENSE file for more information.

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ```
