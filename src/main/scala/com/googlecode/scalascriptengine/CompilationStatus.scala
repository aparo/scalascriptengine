package com.googlecode.scalascriptengine

import java.time.OffsetDateTime
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author kostantinos.kougios
 *
 *         11 Jan 2012
 */
class CompilationStatus private(
                                 val startTime: OffsetDateTime,
                                 val stopTime: Option[OffsetDateTime],
                                 val step: CompilationStatus.Status
                               ) {

  import CompilationStatus._

  private val stopTrigger = new AtomicBoolean(false)

  def stop: Unit = step match {
    case ScanningSources | Compiling => stopTrigger.set(true)
  }

  def stopIfCompiling: Unit = stopTrigger.set(true)

  private[scalascriptengine] def checkStop: Unit = if (stopTrigger.get) throw new CompilationStopped
}

object CompilationStatus {

  abstract class Status

  object NotYetReady extends Status

  object ScanningSources extends Status

  object Compiling extends Status

  object Complete extends Status

  object Failed extends Status

  def notYetReady = new CompilationStatus(OffsetDateTime.now, None, NotYetReady)

  def started = new CompilationStatus(OffsetDateTime.now, None, ScanningSources)

  def failed(currentStatus: CompilationStatus) =
    new CompilationStatus(currentStatus.startTime, Some(OffsetDateTime.now), Failed)

  def completed(currentStatus: CompilationStatus) =
    new CompilationStatus(currentStatus.startTime, Some(OffsetDateTime.now), Complete)
}

class CompilationStopped extends RuntimeException {
  val time = OffsetDateTime.now

  override def getMessage = "compilation stopped at %s".format(time)

  override def toString = "CompilationStopped(%s)".format(time)
}
