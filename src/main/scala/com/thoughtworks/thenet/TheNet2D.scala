package com.thoughtworks.thenet

import java.nio.ByteBuffer
import java.util.concurrent.Semaphore

import com.dongxiguo.fastring.Fastring.Implicits._
import com.dongxiguo.fastring.Fastring
import com.thoughtworks.opencl.Memory.Address
import com.thoughtworks.opencl.OpenCL
import com.thoughtworks.opencl.OpenCL.{Buffer, checkErrorCode}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{Negative, Positive}
import org.lwjgl.opencl.CL10._
import org.lwjgl.opencl.CL12._
import com.thoughtworks.each.Monadic._
import com.thoughtworks.continuation._
import com.thoughtworks.future._

import scalaz.syntax.all._
import com.thoughtworks.raii.asynchronous._

import scala.util.hashing.MurmurHash3
import scalaz.Memo

/**
  * @author 杨博 (Yang Bo)
  */
trait TheNet2D extends AutoCloseable

object TheNet2D {

  object Configuration {
    final case class Point(x: Float, y: Float)
    final case class Fiber(point: Point, neuronOffset: Int Refined Negative)
    final case class Neuron(resolution: Int Refined Positive, fibers: IndexedSeq[Fiber])

  }
  import Configuration._

  object Ast {
//    final case class ForwardKernel(name:String)

  }

  final case class ProgramSource(code: Fastring,
                                 forwardKernelNames: IndexedSeq[String],
                                 backwardKernelNames: IndexedSeq[String],
                                 weightKernalNames: IndexedSeq[String])
  object ProgramSource {
    def generate(hyperparameters: IndexedSeq[Neuron]): ProgramSource = {
      val weightIndices: IndexedSeq[Int] = hyperparameters.scanLeft(0) { (weightIndex, neuron) =>
        weightIndex
      }

      val forwardKernelSources: IndexedSeq[(String, Fastring)] = hyperparameters.toStream.zipWithIndex
        .groupBy(_._1.fibers.map(_.point))
        .toStream
        .zipWithIndex
        .flatMap {
          case ((pixelOffsets, neurons), forwardSourceId) =>
            val kernelName = raw"""forward$forwardSourceId"""
            val code = fast"""
              kernel void $kernelName(read_only image2d_array_t input) {
              }
            """
            neurons.map {
              case (Neuron(resolution, fibers), neuronIndex) =>
                neuronIndex -> (kernelName, code)
            }
        }
        .sortBy(_._1)
        .map(_._2)(collection.breakOut(IndexedSeq.canBuildFrom))

//      hyperparameters.view.zipWithIndex.flatMap {
//        case (neuron, i) =>
//      }

      ???
//    Memo.mutableHashMapMemo
//    val forwardKernelSources = Map
      // val

    }
  }
//
//  def compile(context: OpenCL.Context,
//              commandQueue: OpenCL.CommandQueue,
//              semaphone: Semaphore,
//              hyperparameters: IndexedSeq[TheNet2D.Neuron],
//              batchSize: Int Refined Positive): Do[TheNet2D] = {
////    Do.now(())
//
//    Do.scoped {
//
//        context.createProgramWithSource(hyperparameters.flatMap { neuron =>
//          val forwardKernel: String = ???
//          val backwardKernel: String = ???
//          Seq(forwardKernel, backwardKernel)
//        })
//      }
//      .intransitiveFlatMap { program =>
//        Do.garbageCollected(program.build().map { _ =>
//          try {
//
//            val weights: Seq[OpenCL.Buffer[Float]] = hyperparameters.map { neuron =>
//              context.createBuffer[Float](neuron.fibers.size)
//            }
//
//            val forwardBuffers: IndexedSeq[OpenCL.Image] = hyperparameters.map { neuron =>
//              context.createImage(width = neuron.resolution.value.toLong,
//                                  height = neuron.resolution.value.toLong,
//                                  arraySize = batchSize.value)
//            }
//            val backwardBuffers: IndexedSeq[OpenCL.Image] = hyperparameters.map { neuron =>
//              context.createImage(width = neuron.resolution.value.toLong,
//                                  height = neuron.resolution.value.toLong,
//                                  arraySize = batchSize.value)
//            }
//            val forwardKernels: IndexedSeq[OpenCL.Kernel] = ???
//            val backwardKernels: IndexedSeq[OpenCL.Kernel] = ???
//            new TheNet2D {
//
//              override def close(): Unit = {
//                // TODO: close weights
//              }
//            }
//
//          } finally {
//            program.close()
//          }
//
//        })
//      }
//  }
}
