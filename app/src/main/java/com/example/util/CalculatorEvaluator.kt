package com.example.util

object CalculatorEvaluator {

    /**
     * Evaluates arithmetic expressions containing numbers, +, -, *, /, %, and decimals.
     * Supports standard calculator percentage rules:
     * - Multiplications with percentage: e.g. 1000 * 1.85% = 18.5, 1200 * 1.85% = 22.2
     * - Additions/Subtractions with percentage: e.g. 100 + 10% = 110.0, 1000 - 5% = 950.0
     * - Standalone percentage: e.g. 50% = 0.5, 1.85% = 0.0185
     * - Divisions with percentage: e.g. 100 / 50% = 200.0
     * Returns Result with Double value or Error message.
     */
    fun evaluate(expression: String): Result<Double> {
        val clean = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")
            .trim()

        if (clean.isEmpty()) return Result.success(0.0)

        // Strip trailing incomplete binary operators for live preview if user is currently typing
        val parsable = clean.trimEnd('+', '-', '*', '/')
        if (parsable.isEmpty()) return Result.success(0.0)

        return try {
            val result = Parser(parsable).parse()
            if (result.isNaN() || result.isInfinite()) {
                Result.failure(IllegalArgumentException("Invalid calculation"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class Parser(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: $ch")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> {
                        val beforePos = pos
                        val (rightVal, hadPercent) = parseTermWithPercentCheck()
                        if (hadPercent) {
                            // If percentage in addition (e.g. 100 + 10%), it adds (x * rightVal) where rightVal was divided by 100
                            x += (x * (rightVal * 100.0) / 100.0)
                        } else {
                            x += rightVal
                        }
                    }
                    eat('-') -> {
                        val (rightVal, hadPercent) = parseTermWithPercentCheck()
                        if (hadPercent) {
                            // If percentage in subtraction (e.g. 100 - 10%), it subtracts (x * percentage)
                            x -= (x * (rightVal * 100.0) / 100.0)
                        } else {
                            x -= rightVal
                        }
                    }
                    else -> return x
                }
            }
        }

        private fun parseTermWithPercentCheck(): Pair<Double, Boolean> {
            val startPos = pos
            val rightTerm = parseTerm()
            val segment = if (startPos in 0..str.length && pos in 0..str.length && startPos <= pos) {
                str.substring(startPos, pos)
            } else ""
            val hadPercent = segment.contains("%")
            return Pair(rightTerm, hadPercent)
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> {
                        val factor = parseFactor()
                        x *= factor
                    }
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> {
                        x /= 100.0
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = str.substring(startPos, pos)
                x = numStr.toDoubleOrNull() ?: 0.0
                if (eat('%')) {
                    x /= 100.0
                }
            } else {
                throw RuntimeException("Unexpected character: $ch")
            }
            return x
        }
    }
}
