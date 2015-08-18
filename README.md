Galeb Manager
===========================
[![Build Status](https://travis-ci.org/galeb/galeb-manager.svg)](https://travis-ci.org/galeb/galeb-manager)

Galeb manager is a galeb router manager (multi farms/cluster) built on Spring Boot.<br/>
Galeb Router is a massively parallel routing system running a shared-nothing architecture.

Its main features are:
* Multi Driver (Galeb Router Farm only for now)
* Open Source
* API REST (management)
* Allows dynamically change routes and configuration without having to restart or reload
* Highly scalable

Building
-----

<code>
$ mvn clean package
</code><br/>

Using
-----

<code>
$ mvn exec:exec
</code><br/>

