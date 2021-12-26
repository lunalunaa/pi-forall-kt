package org.luna.piforall.parse

import com.github.h0tk3y.betterParse.combinators.MapCombinator
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.ParseResult
import com.github.h0tk3y.betterParse.parser.Parsed
import com.github.h0tk3y.betterParse.parser.Parser

// from Pull Request #6 of better-parse

/** Parses the sequence with [innerParser], and if that succeeds, maps its [Parsed] result with [transform].
 * Then run this mapped result on any remaining input.
 * Returns the [ErrorResult] of the `innerParser` otherwise.
 */
class BindCombinator<T, R>(
  private val innerParser: Parser<T>,
  val transform: (T) -> Parser<R>,
) : Parser<R> {
  override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<R> =
    when (val innerResult = innerParser.tryParse(tokens, fromPosition)) {
      is ErrorResult -> innerResult
      is Parsed -> transform(innerResult.value).tryParse(tokens, innerResult.nextPosition)
    }
}

/** Applies the [transform] function to the successful results of the receiver parser. See [MapCombinator]. */
infix fun <A, T> Parser<A>.bind(transform: (A) -> Parser<T>): Parser<T> = BindCombinator(this, transform)

/** Applies the [transform] extension to the successful results of the receiver parser. See [MapCombinator]. */
infix fun <A, T> Parser<A>.useBind(transform: A.() -> Parser<T>): Parser<T> = BindCombinator(this, transform)

/** Returns [Parsed] of [pureValue] without consuming any input */
class PureCombinator<T>(private val pureValue: T) : Parser<T> {
  override fun tryParse(tokens: TokenMatchesSequence, fromPosition: Int): ParseResult<T> = object : Parsed<T>() {
    override val nextPosition: Int
      get() = fromPosition
    override val value: T
      get() = pureValue
  }
}

/** Returns [Parsed] of [value] without consuming any input */
fun <T> pure(value: T): Parser<T> = PureCombinator(value)
