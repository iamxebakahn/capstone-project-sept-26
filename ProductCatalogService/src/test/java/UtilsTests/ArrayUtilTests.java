
package UtilsTests;

import dev.zeba.productcatalogservice.util.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayUtilTests {
    //happy case: func name should be what action you perform and what result you expect
    @Test
    @DisplayName("When there's middle index then get valid value") // when my test case fails, instead of function name this is displayed
    void getElement_middleIndex_thenReturnElement(){
        int shelf[] = {10,20,30}; //this line's preparing input
        int actual = ArrayUtils.getElementByIndex(shelf,1);  //this line's acting on it
        assertEquals(20,actual); //this line's asserting /verifying
        //framework to write test case: AAA philosophy : Arrange: prepare i/p, Act: actually calling i/p, Assert: verifying whether test is possible
    }

    @Test
    @DisplayName("When there's negative index,then throw Exception")
    void getElement_negativeIndex_thenThrowException() {
        int shelf[] = {10,20,30};
        assertThrows(IllegalArgumentException.class, ()-> ArrayUtils.getElementByIndex(shelf,-1));
    }

    //to check for all indexes of array, we can write 3 test cases for same array, instead of writing so many test cases,
    //we can write parameterised test cases
    @ParameterizedTest
    @DisplayName("When there's a valid index then get a valid value")
    @CsvSource({"0,10","1,20","2,30"}) // test would run 3 times
    void getElement_valideIndex_thenReturnElement(int index, int expected){ //since this pair needs to be given, use @csvsource
        int shelf[] = {10,20,30};
        int actual = ArrayUtils.getElementByIndex(shelf,index);
        assertEquals(expected,actual);
    }

    //Test case coverage: how many lines are covered by the test cases! in ideal world 100% lines to be covered
    //threshold decided by team - 80-90%, plugin Jacoco to find out how many lines are written or generate a test report
    //deos coverage means correctness of test cases? if someone has covered 90% coverage, does it mean they have tested all possible cases?
    //is coverage = correctness? no:
}


