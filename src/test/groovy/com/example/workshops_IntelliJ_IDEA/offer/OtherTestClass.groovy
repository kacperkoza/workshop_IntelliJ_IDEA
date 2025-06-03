package com.example.workshops_IntelliJ_IDEA.offer

import spock.lang.Specification
import spock.lang.Unroll


class OtherTestClass extends Specification {

    @Unroll
    def "should manipulate string"() {
        expect:
        "hello".toUpperCase() == "HELLO"
    }

    @Unroll
    def "should find max value with where clause"() {
        expect:
        Math.max(a, b) == result

        where:
        a | b || result
        1 | 2 || 2
        3 | 1 || 3
        5 | 5 || 5
    }

    @Unroll
    def "should add values with where clause"() {
        expect:
        a + b == result

        where:
        a | b || result
        1 | 2 || 3
        3 | 4 || 7
        5 | 6 || 11
    }

    @Unroll
    def "should add two numbers"() {
        expect:
        1 + 1 == 2
    }

    @Unroll
    def "should get string length"() {
        expect:
        "Spock".length() == 5
    }

}