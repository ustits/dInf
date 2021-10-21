package dinf.types

import kotlin.jvm.JvmInline

@JvmInline
value class PositiveInt private constructor(val value: Int) {

    companion object {

        fun orNull(int: Int): PositiveInt? =
            int.takeIf { int > 0 }?.let { PositiveInt(it) }

    }

}