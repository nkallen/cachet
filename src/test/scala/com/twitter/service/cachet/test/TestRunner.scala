package com.twitter.service.cachet.test

import org.specs.runner.SpecsFileRunner
import net.lag.logging.Logger

object TestRunner extends SpecsFileRunner("src/test/scala/**/*.scala",
  System.getProperty("spec", ".*"), System.getProperty("system", ".*"), System.getProperty("example", ".*"))
