package module3

import zio.clock.Clock
import zio.console.{Console, putStrLn}
import zio.{Has, URIO, ZIO, ZLayer}
import zio.macros.accessible

import java.util.concurrent.TimeUnit

package object benchmarkService {
  type BenchmarkService = Has[BenchmarkService.Service]

  @accessible
  object BenchmarkService{

    trait Service{
      def currentTime: URIO[Clock, Long]
      def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A]
    }

    class BenchmarkServiceImpl(clock: Clock.Service) extends Service {
      override def currentTime: URIO[Clock, Long] = clock.currentTime(TimeUnit.SECONDS)

      override def printEffectRunningTime[R, E, A](zio: ZIO[R, E, A]): ZIO[Console with Clock with R, E, A] = for{
        start <- currentTime
        r <- zio
        finish <- currentTime
        _ <- putStrLn(s"Running time ${finish - start}")
      } yield r
    }

    val live = ZLayer.fromService[Clock.Service, BenchmarkService.Service]((clock) => new BenchmarkServiceImpl(clock))
  }
}
