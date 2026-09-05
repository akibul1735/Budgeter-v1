package com.example.util

object CalculatorEvaluator {

    /**
     * Evaluates arithmetic expressions containing numbers, +, -, *, /, %, and decimals.
     * Supports standard calculator percentage rules (e.g., 100 + 10% = 110, 100 * 10% = 10, 50% = 0.5).
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

        // Strip trailing operator for live preview if user is currently typing
        val parsable = clean.trimEnd('+', '-', '*', '/', '%')
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
                        val rightTerm = parseTerm()
                        // Check if the term had a percentage applied
                        if (beforePos < str.length && str.substring(beforePos - 1).contains("%")) {
                            x += rightTerm
                        } else if (hasPercentageInSegment(beforePos)) {
                            x += (x * rightTerm)
                        } else {
                            x += rightTerm
                        }
                    }
                    eat('-') -> {
                        val beforePos = pos
                        val rightTerm = parseTerm()
                        if (beforePos < str.length && str.substring(beforePos - 1).contains("%")) {
                            x -= rightTerm
                        } else if (hasPercentageInSegment(beforePos)) {
                            x -= (x * rightTerm)
                        } else {
                            x -= rightTerm
                        }
                    }
                    else -> return x
                }
            }
        }

        private fun hasPercentageInSegment(startIdx: Int): Boolean {
            val endIdx = (pos).coerceAtMost(str.length)
            if (startIdx in 0..endIdx) {
                return str.substring(startIdx, endIdx).contains("%")
            }
            return false
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> {
                        x = x / 100.0
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
