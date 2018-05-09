package com.thoughtworks.deeplearning.etl

import java.net.URL
import java.nio.MappedByteBuffer
import java.nio.channels.{Channels, FileChannel}
import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import com.thoughtworks.compute.Tensors
import com.thoughtworks.future._
import com.thoughtworks.raii.asynchronous._
import com.thoughtworks.dsl.domains.scalaz._
import com.thoughtworks.dsl.keywords.Monadic._
import org.rauschig.jarchivelib.{Archiver, ArchiverFactory}

import scala.util.Random

trait Cifar10Loader extends Tensors {
  import Cifar10Loader._

  final case class Cifar10(trainBuffers: Seq[MappedByteBuffer], testBuffer: MappedByteBuffer) {

    import Cifar10._

    /** file 里有多少个图像 */
    private def numberOfTrainSamplesPerFile = trainBuffers.head.capacity / NumberOfBytesPerSample
    private def numberOfTestSamplesPerFile = testBuffer.capacity / NumberOfBytesPerSample
    if (trainBuffers.map(_.capacity).toSet.size != 1) {
      throw new IllegalArgumentException("Train files should not have different sizes.")
    }

    if (trainBuffers.head.capacity % NumberOfBytesPerSample != 0) {
      throw new IllegalArgumentException(s"Train files' size must be $NumberOfBytesPerSample multiple.")
    }

    /** total 图像数 */
    private def numberOfTrainSamples = numberOfTrainSamplesPerFile * trainBuffers.length
    private def numberOfTestSamples = numberOfTestSamplesPerFile

    def epoch(batchSize: Int): Iterator[Batch] = {
      Random.shuffle[Int, IndexedSeq](0 until numberOfTrainSamples).grouped(batchSize).map { batchIndices =>
        loadBatch(batchSize, batchIndices)
      }
    }

    def testBatches(batchSize: Int): Iterator[Batch] = {
      (0 until numberOfTestSamples).grouped(batchSize).map { batchIndices =>
        loadTestBatch(batchSize, batchIndices)
      }
    }

    private def batchOneHotEncoding(label: Array[Int], numberOfClasses: Int): Tensor = {
      val batchSize = label.length
      val encoded = Array.ofDim[Float](batchSize, numberOfClasses)
      for (i <- label.indices) {
        encoded(i)(label(i)) = 1.0f
      }
      Tensor(encoded)
    }

    private def loadTestBatch(batchSize: Int, batchIndices: IndexedSeq[Int]): Batch = {

      val (labels, pixels) = (for (testImageIndex <- batchIndices) yield {
        val offset = testImageIndex * NumberOfBytesPerSample

        val label = testBuffer.get(offset) & 0xff
        val imageArray = Array.ofDim[Byte](NumberOfPixelsPerSample)

        testBuffer.position(offset + 1)
        testBuffer.get(imageArray)
        val pixels: Array[Float] = for (pixel <- imageArray) yield {
          ((pixel & 0xff).toFloat + 0.5f) / 256.0f
        }

        (label, pixels)
      })(collection.breakOut(Array.canBuildFrom)).unzip

      Batch(
        batchOneHotEncoding(labels, NumberOfClasses),
        Tensor(pixels).reshape(Array(pixels.length, NumberOfChannels, Width, Height))
      )
    }

    private[etl] def loadBatch(batchSize: Int, batchIndices: IndexedSeq[Int]): Batch = {

      val (labels, pixels) = (for (trainImageIndex <- batchIndices) yield {
        val offset = trainImageIndex % numberOfTrainSamplesPerFile * NumberOfBytesPerSample
        val fileIndex = trainImageIndex / numberOfTrainSamplesPerFile

        val label = trainBuffers(fileIndex).get(offset) & 0xff
        val imageArray = Array.ofDim[Byte](NumberOfPixelsPerSample)

        trainBuffers(fileIndex).position(offset + 1)
        trainBuffers(fileIndex).get(imageArray)
        val pixels: Array[Float] = for (pixel <- imageArray) yield {
          ((pixel & 0xff).toFloat + 0.5f) / 256.0f
        }

        (label, pixels)
      })(collection.breakOut(Array.canBuildFrom)).unzip

      Batch(
        batchOneHotEncoding(labels, NumberOfClasses),
        Tensor(pixels).reshape(Array(pixels.length, NumberOfChannels, Width, Height))
      )
    }
  }

  final case class Batch(labels: Tensor, pixels: Tensor)

  def load(url: URL = new URL("http://www.cs.toronto.edu/~kriz/cifar-10-binary.tar.gz")): Future[Cifar10] =
    Future.delay {
      if (!Files.exists(extractedDataPath)) {
        val targzPath = Files.createTempFile("cifar-10-binary", ".tar.gz")

        def download = Do.delay {
          val cifarHttpStream = !Do.autoCloseable(url.openStream())
          val httpChannel = !Do.autoCloseable(Channels.newChannel(cifarHttpStream))
          val fileChannel = !Do.autoCloseable(FileChannel.open(targzPath, StandardOpenOption.WRITE))

          fileChannel.transferFrom(httpChannel, 0, Long.MaxValue)
        }

        !download.run
        val archiver: Archiver = ArchiverFactory.createArchiver("tar", "gz")
        archiver.extract(targzPath.toFile, cacheDirectory.toFile)
        extractedDataPath.ensuring(Files.exists(_))
      }

      val trainBuffers = for {
        i <- 1 to 5
      } yield {
        mapBuffer(extractedDataPath.resolve(s"data_batch_$i.bin"))
      }
      val testBuffer = mapBuffer(extractedDataPath.resolve("test_batch.bin"))

      Cifar10(trainBuffers, testBuffer)
    }
}

/**
  * @author 杨博 (Yang Bo)
  */
object Cifar10Loader {

  val Width = 32

  val Height = 32

  val NumberOfChannels = 3

  val NumberOfPixelsPerSample = Width * Height * NumberOfChannels

  val NumberOfLabelsPerSample = 1

  val NumberOfBytesPerSample = NumberOfLabelsPerSample + NumberOfPixelsPerSample

  val NumberOfClasses = 10

  private val cacheDirectory = Paths.get(sys.props("user.home"), ".cifar10")

  private val extractedDataPath = cacheDirectory.resolve("cifar-10-batches-bin")

  private def mapBuffer(path: Path) = {
    val channel = FileChannel.open(path, StandardOpenOption.READ)
    try {
      channel.map(FileChannel.MapMode.READ_ONLY, 0L, channel.size)
    } finally {
      channel.close()
    }
  }
}
