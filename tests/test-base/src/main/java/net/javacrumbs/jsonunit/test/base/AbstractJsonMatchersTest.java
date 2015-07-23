/**
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.jsonunit.test.base;

import net.javacrumbs.jsonunit.JsonAssert;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static java.util.Arrays.asList;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodeAbsent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodePresent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonStringEquals;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonStringPartEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public abstract class AbstractJsonMatchersTest {
    @After
    public void reset() {
        JsonAssert.setTolerance(null);
    }

    @Test
    public void testEquals() {
        assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 1\n}"));
        assertThat("{\"test\":1}", not(jsonEquals("{\n\"test\": 2\n}")));
        assertThat("{\"test\":1}", jsonPartEquals("test", "1"));
        assertThat("{\"test\":1}", jsonPartEquals("test", 1));
        assertThat("{\"test\":[1, 2, 3]}", jsonPartEquals("test[0]", "1"));
        assertThat("{\"foo\":\"bar\",\"test\": 2}", jsonEquals("{\n\"test\": 2,\n\"foo\":\"bar\"}"));
        assertThat("{}", jsonEquals("{}"));
    }

    @Test
    public void shouldNotFailOnEmptyInput() {
        try {
            assertThat("", jsonEquals("{\"test\":1}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: {\"test\":1}\n" +
                    "     but: JSON documents are different:\n" +
                    "Different value found in node \"\". Expected '{\"test\":1}', got '\"\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testGenericsStringInference() {
        doAssertThat("{\"test\":1}", jsonStringPartEquals("test", "1"));
        doAssertThat("{\"test\":1}", jsonStringEquals("{\"test\" : 1}"));
        //doAssertThat("{\"test\":1}", jsonPartEquals("test", "1")); //does not compile in Java 7
    }

    private void doAssertThat(String text, Matcher<String> matcher) {
        assertThat(text, matcher);
    }

    @Test
    public void testToleranceStatic() throws IOException {
        JsonAssert.setTolerance(0.001);
        assertThat("{\"test\":1.00001}", jsonEquals("{\"test\":1}"));
    }

    @Test
    public void testTolerance() throws IOException {
        assertThat("{\"test\":1.00001}", jsonEquals("{\"test\":1}").withTolerance(0.001).when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void shouldIgnoreExtraFields() {
        assertThat("{\"test\":{\"a\":1, \"b\":2, \"c\":3}}", jsonEquals("{\"test\":{\"b\":2}}").when(IGNORING_EXTRA_FIELDS));
    }

    @Test
    public void hasItemShouldWork() throws IOException {
        //assertThat(asList("{\"test\":1}"), hasItem(jsonEquals("{\"test\":1}"))); //does not compile
        assertThat(asList("{\"test\":1}"), contains(jsonEquals("{\"test\":1}")));
    }

    @Test
    public void testAssertDifferentTypeInt() {
        try {
            assertThat("{\"test\":\"1\"}", jsonPartEquals("test", 1));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\n" +
                    "Expected: 1 in \"test\"\n" +
                    "     but: JSON documents are different:\n" +
                    "Different value found in node \"test\". Expected '1', got '\"1\"'.\n", e.getMessage());
        }
    }

    @Test
    public void testGenericsInt() {
        Matcher<Integer> intMatcher = jsonEquals(1);
        assertThat(1, intMatcher);
    }

    @Test
    public void testGenericsIntAndString() {
        Matcher<String> stringMatcher = jsonPartEquals("test", 1);
        assertThat("{\"test\":1}", stringMatcher);
    }


    @Test
    public void testDifferentValue() {
        try {
            assertThat("{\"test\":1}", jsonEquals("{\n\"test\": 2\n}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: {\n\"test\": 2\n}\n" +
                    "     but: JSON documents are different:\n" +
                    "Different value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentStructure() {
        try {
            assertThat("{\"test\":1}", jsonEquals("{\n\"test2\": 2\n}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: {\n\"test2\": 2\n}\n" +
                    "     but: JSON documents are different:\n" +
                    "Different keys found in node \"\". Expected [test2], got [test]. Missing: \"test2\" Extra: \"test\"\n", e.getMessage());
        }
    }

    @Test
    public void testDifferentPartValue() {
        try {
            assertThat("{\"test\":1}", jsonPartEquals("test", "2"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\nExpected: 2 in \"test\"\n" +
                    "     but: JSON documents are different:\n" +
                    "Different value found in node \"test\". Expected 2, got 1.\n", e.getMessage());
        }
    }

    @Test
    public void testAbsent() {
        try {
            assertThat("{\"test\":1}", jsonNodeAbsent("test"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\n" +
                    "Expected: Node \"test\" is absent.\n" +
                    "     but: Node \"test\" is \"1\".", e.getMessage());
        }
    }

    @Test
    public void testAbsentOk() {
        assertThat("{\"test\":1}", jsonNodeAbsent("different"));
    }

    @Test
    public void testPresent() {
        try {
            assertThat("{\"test\":1}", jsonNodePresent("test.a"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\n" +
                    "Expected: Node \"test.a\" is present.\n" +
                    "     but: Node \"test.a\" is missing.", e.getMessage());
        }
    }

    @Test
    public void testPresentOk() {
        assertThat("{\"test\":1}", jsonNodePresent("test"));
    }

    @Test
    public void testNullAndAbsent() throws IOException {
        try {
            assertThat("{\"test\":{\"a\":1, \"b\": null}}", jsonEquals("{\"test\":{\"a\":1}}"));
            fail("Exception expected");
        } catch (AssertionError e) {
            assertEquals("\n" +
                    "Expected: {\"test\":{\"a\":1}}\n" +
                    "     but: JSON documents are different:\n" +
                    "Different keys found in node \"test\". Expected [a], got [a, b].  Extra: \"test.b\"\n", e.getMessage());
        }
    }

    @Test
    public void shouldIgnoreValues() {
        assertThat("{\"test\":{\"a\":3,\"b\":2,\"c\":1}}", jsonEquals("{\"test\":{\"a\":1,\"b\":2,\"c\":3}}").when(IGNORING_VALUES));
    }

    @Test
    public void testTreatNullAsAbsent() {
        JsonAssert.setOptions(TREATING_NULL_AS_ABSENT);
        assertThat("{\"test\":{\"a\":1, \"b\": null}}", jsonEquals("{\"test\":{\"a\":1}}"));
    }

    @Test
    public void testJsonNode() throws IOException {
        assertThat(readValue("{\"test\":1}"), jsonEquals("{\"test\":1}"));
    }

    protected abstract Object readValue(String value);
}