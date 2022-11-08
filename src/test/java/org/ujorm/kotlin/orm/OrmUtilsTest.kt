package org.ujorm.kotlin.orm

import ch.tutteli.atrium.api.fluent.en_GB.toEqual
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

internal class OrmUtilsTest {


    @Test
    fun toSnackCase() {
        val u = OrmUtils

        expect(u.toSnackCase("abc")).toEqual("abc")
        expect(u.toSnackCase(" abc ")).toEqual("abc")
        expect(u.toSnackCase("aBc")).toEqual("a_bc")
        expect(u.toSnackCase("abC")).toEqual("ab_c")
        expect(u.toSnackCase("ábČ")).toEqual("áb_č")
        expect(u.toSnackCase("aBC")).toEqual("a_bc")
        expect(u.toSnackCase("ABC")).toEqual("_abc")

        expect(u.toSnackCase("a2c")).toEqual("a2c")
        expect(u.toSnackCase(" a2c ")).toEqual("a2c")
        expect(u.toSnackCase("a2c")).toEqual("a2c")
        expect(u.toSnackCase("a2C")).toEqual("a2_c")
        expect(u.toSnackCase("á2Č")).toEqual("á2_č")
        expect(u.toSnackCase("a2C")).toEqual("a2_c")
        expect(u.toSnackCase("A2C")).toEqual("_a2_c")

        expect(u.toSnackCase("a b c")).toEqual("a_b_c")
        expect(u.toSnackCase("a b  c")).toEqual("a_b_c")
        expect(u.toSnackCase("a B\tC")).toEqual("a_b_c")
        expect(u.toSnackCase("a b\tC")).toEqual("a_b_c")

        expect(u.toSnackCase("a_b_c")).toEqual("a_b_c")
        expect(u.toSnackCase("a_b_c")).toEqual("a_b_c")
        expect(u.toSnackCase("a_B__C")).toEqual("a_b_c")
        expect(u.toSnackCase("a_b___C")).toEqual("a_b_c")
        expect(u.toSnackCase("_a_b_c_")).toEqual("_a_b_c")
    }
}