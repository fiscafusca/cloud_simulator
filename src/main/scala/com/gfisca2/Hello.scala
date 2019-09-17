package com.gfisca2

import java.util.Calendar

import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.core.CloudSim
import org.slf4j.{Logger, LoggerFactory}

object Hello {

  val LOG: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {
    println("Hello, world")

    val conf = ConfigFactory.load();
    val testParam = conf.getInt("hw1.testParam");
    println(testParam)

    LOG.trace("Hello World!")
    LOG.debug("How are you today?")
    LOG.info("I am fine.")
    LOG.warn("I love programming.")
    LOG.error("I am programming.")

    val num_user = 1 // number of cloud users
    val calendar = Calendar.getInstance // Calendar whose fields have been initialized with the current date and time.
    val trace_flag = false // trace events

    CloudSim.init(num_user, calendar, trace_flag)

  }
}