package com.switchcase.asyncthroughput

import spock.lang.Specification


class BigDecimalTest extends Specification {

    def "test double constructor"() {
        when:
        BigDecimal val = new BigDecimal(0.1).add(new BigDecimal(0.1)).add(new BigDecimal(0.1));

        then:
        val.toString() != "0.3"
    }


    def "test string constructor"() {
        when:
        BigDecimal val = new BigDecimal("0.1").add(new BigDecimal("0.1")).add(new BigDecimal("0.1"));

        then:
        val.toString() == "0.3"
    }

}
