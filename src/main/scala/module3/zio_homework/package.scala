package module3

import module3.zioConcurrency.printEffectRunningTime
import zio.clock.{Clock, sleep}
import zio.{ZIO}
import zio.console.{Console, putStrLn}
import zio.duration.durationInt
import zio.random.{Random, nextIntBetween}

import scala.io.StdIn
import scala.language.postfixOps

package object zio_homework {
    /**
   * 1.
   * Используя сервисы Random и Console, напишите консольную ZIO программу которая будет предлагать пользователю угадать число от 1 до 3
   * и печатать в когнсоль угадал или нет.
   */

  lazy val readInt: ZIO[Console, Throwable, Int] = ZIO.effect(StdIn.readLine()).flatMap(str => ZIO.effect(str.toInt))
  lazy val win: ZIO[Random with Console, Throwable, Unit] = ZIO.effect(println("Ты угадал! Попробуй снова")) *> guessProgram
  lazy val lose: ZIO[Random with Console, Throwable, Unit] = ZIO.effect(println("Не угадал. Попробуй снова")) *> guessProgram
  lazy val guessProgram: ZIO[Random with Console, Throwable, Unit] = readInt.flatMap((value: Int) => for {
    rand <- nextIntBetween(1, 4)
    _ <- if (rand == value) win else lose
  } yield ())

  /**
   * 2. реализовать функцию doWhile, которая будет выполнять эффект до тех пор, пока его значение в условии не даст true
   */

  def doWhile[R, E, A](body: ZIO[R, E, A])(condition: A => Boolean): ZIO[R, E, A] = body.repeatWhile(condition)

  /**
   * 3. Реализовать метод, который безопасно прочитает конфиг из файла, а в случае ошибки вернет дефолтный конфиг
   * и выведет его в консоль
   * Используйте эффект "load" из пакета config
   */

  def loadConfigOrDefault: ZIO[Any, Throwable, config.AppConfig] =
    config.load.onError(_ => ZIO.succeed(config.AppConfig("Default name", "Default url")))

  /**
   * 4. Следуйте инструкциям ниже для написания 2-х ZIO программ,
   * обратите внимание на сигнатуры эффектов, которые будут у вас получаться,
   * на изменение этих сигнатур
   */


  /**
   *  4.1 Создайте эффект, который будет возвращать случайеым образом выбранное число от 0 до 10 спустя 1 секунду
   *  Используйте сервис zio Random
   */
  lazy val eff: ZIO[Random with Clock, Nothing, Int] = sleep(1.second) *> nextIntBetween(0, 10)

  /**
   * 4.2 Создайте коллукцию из 10 выше описанных эффектов (eff)
   */
  lazy val effects: List[ZIO[Random with Clock, Nothing, Int]] = List.fill(10)(eff)

  /**
   * 4.3 Напишите программу которая вычислит сумму элементов коллекци "effects",
   * напечатает ее в консоль и вернет результат, а также залогирует затраченное время на выполнение,
   * можно использовать ф-цию printEffectRunningTime, которую мы разработали на занятиях
   */

  lazy val app: ZIO[Console with Clock with Random, Nothing, Unit] = printEffectRunningTime(
      effects.fold(ZIO.succeed(0))((acc, item) => {
        acc.zipWith(item)((sum, el) => sum + el)
      })
    ).flatMap(result => putStrLn(s"Result: $result"))

  /**
   * 4.4 Усовершенствуйте программу 4.3 так, чтобы минимизировать время ее выполнения
   */

    lazy val appSpeedUp: ZIO[Console with Clock with Random, Nothing, Unit] = printEffectRunningTime(
      effects.fold(ZIO.succeed(0))((acc, item) => {
        acc.zipWithPar(item)((sum, el) => sum + el)
      })
    ).flatMap(result => putStrLn(s"Result: $result"))


  /**
   * 5. Оформите ф-цию printEffectRunningTime разработанную на занятиях в отдельный сервис, так чтобы ее
   * молжно было использовать аналогично zio.console.putStrLn например
   */
}
